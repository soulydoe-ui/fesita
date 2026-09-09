package com.fiestast.launcher.android.navigation

import android.app.Notification
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.service.notification.StatusBarNotification
import android.util.Log
import com.fiestast.launcher.android.notifications.NotificationListenerBridge
import com.fiestast.launcher.data.repository.LauncherPreferencesRepository
import com.fiestast.launcher.data.repository.SharedPreferencesLauncherRepository
import com.fiestast.launcher.domain.model.NavigationAppInfo
import com.fiestast.launcher.domain.model.NavigationState
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.NavigationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidNavigationService(
    private val context: Context,
    private val preferencesRepository: LauncherPreferencesRepository = SharedPreferencesLauncherRepository(context)
) : NavigationService, NotificationListenerBridge.NotificationListenerCallback {

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Available("Navigation service ready")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _defaultNavigationApp = MutableStateFlow<String?>(null)
    override val defaultNavigationApp: StateFlow<String?> = _defaultNavigationApp.asStateFlow()

    private val _navigationApps = MutableStateFlow<List<NavigationAppInfo>>(emptyList())
    override val navigationApps: StateFlow<List<NavigationAppInfo>> = _navigationApps.asStateFlow()

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    override val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    @Volatile
    private var activeNavigatingPackage: String? = null

    init {
        NotificationListenerBridge.register(this)
        refreshNavigationApps()
        checkActiveGuidanceNotifications()
    }

    override fun refreshNavigationApps() {
        try {
            val pm = context.packageManager
            val detected = mutableMapOf<String, String>()

            // 1. Query apps handling geo:0,0?q=
            val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
            val resolvedActivities = pm.queryIntentActivities(geoIntent, 0)
            for (resolveInfo in resolvedActivities) {
                val pkg = resolveInfo.activityInfo?.packageName ?: continue
                if (!detected.containsKey(pkg)) {
                    val label = try {
                        resolveInfo.loadLabel(pm).toString()
                    } catch (_: Throwable) {
                        pkg
                    }
                    detected[pkg] = label
                }
            }

            // 2. Query known automotive and popular navigation packages
            val knownPackages = listOf(
                "com.google.android.apps.maps",
                "com.waze",
                "net.osmand",
                "net.osmand.plus",
                "com.sygic.aura",
                "com.here.app.maps",
                "com.tomtom.gplay.navapp"
            )

            for (pkg in knownPackages) {
                if (!detected.containsKey(pkg)) {
                    try {
                        val appInfo = pm.getApplicationInfo(pkg, 0)
                        val label = pm.getApplicationLabel(appInfo).toString()
                        detected[pkg] = label
                    } catch (_: PackageManager.NameNotFoundException) {
                        // App is not installed, skip
                    } catch (_: Throwable) {}
                }
            }

            val savedPreferred = preferencesRepository.preferences.value.preferredNavigationPackage

            val appList = detected.map { (pkg, label) ->
                val isPreferred = when {
                    savedPreferred != null -> pkg == savedPreferred
                    detected.size == 1 -> true
                    else -> false
                }
                NavigationAppInfo(
                    packageName = pkg,
                    label = label,
                    isPreferred = isPreferred
                )
            }.sortedWith(
                compareByDescending<NavigationAppInfo> { it.isPreferred }
                    .thenBy { it.label.lowercase() }
            )

            _navigationApps.value = appList

            val resolvedDefault = when {
                savedPreferred != null && detected.containsKey(savedPreferred) -> savedPreferred
                appList.size == 1 -> appList.first().packageName
                appList.isNotEmpty() -> appList.firstOrNull { it.isPreferred }?.packageName ?: appList.first().packageName
                else -> null
            }
            _defaultNavigationApp.value = resolvedDefault

            if (appList.isEmpty()) {
                _status.value = ServiceStatus.Unavailable("No navigation application installed")
                if (_navigationState.value !is NavigationState.Navigating) {
                    _navigationState.value = NavigationState.Unavailable("No navigation app installed")
                }
            } else {
                _status.value = ServiceStatus.Available("Ready with ${appList.size} navigation apps")
                if (_navigationState.value !is NavigationState.Navigating) {
                    _navigationState.value = NavigationState.Idle
                }
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Error refreshing navigation apps: ${e.message}")
        }
    }

    override fun setPreferredNavigationApp(packageName: String) {
        preferencesRepository.setPreferredNavigationPackage(packageName)
        refreshNavigationApps()
    }

    override fun launchNavigation(): Boolean = launchNavigationApp()

    override fun launchNavigationApp(): Boolean {
        val targetPackage = _defaultNavigationApp.value
            ?: preferencesRepository.preferences.value.preferredNavigationPackage
            ?: _navigationApps.value.firstOrNull()?.packageName

        return try {
            val pm = context.packageManager
            val intent = if (targetPackage != null) {
                val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                        setPackage(targetPackage)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: Throwable) {
            false
        }
    }

    override fun searchDestination(query: String): Boolean {
        if (query.isBlank()) return false
        val targetPackage = _defaultNavigationApp.value
            ?: preferencesRepository.preferences.value.preferredNavigationPackage
            ?: _navigationApps.value.firstOrNull()?.packageName

        val encoded = Uri.encode(query)
        val uri = Uri.parse("geo:0,0?q=$encoded")

        return try {
            val pm = context.packageManager
            var intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (targetPackage != null) {
                    setPackage(targetPackage)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(pm) == null && targetPackage != null) {
                // Fallback without package constraint
                intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    }

    override fun openNavigationApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null && launchIntent.resolveActivity(pm) != null) {
                context.startActivity(launchIntent)
                true
            } else {
                val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (geoIntent.resolveActivity(pm) != null) {
                    context.startActivity(geoIntent)
                    true
                } else {
                    false
                }
            }
        } catch (_: Throwable) {
            false
        }
    }

    override fun cleanup() {
        NotificationListenerBridge.unregister(this)
    }

    // NotificationListenerBridge callbacks
    override fun onListenerConnected() {
        checkActiveGuidanceNotifications()
    }

    override fun onListenerDisconnected() {
        // Safe disconnect
    }

    override fun onNotificationChanged() {
        checkActiveGuidanceNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        parseNavigationNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName == activeNavigatingPackage) {
            activeNavigatingPackage = null
            _navigationState.value = if (_navigationApps.value.isEmpty()) {
                NavigationState.Unavailable("No navigation application installed")
            } else {
                NavigationState.Idle
            }
        }
    }

    private fun checkActiveGuidanceNotifications() {
        val activeNotifs = NotificationListenerBridge.getActiveNotifications() ?: return
        for (sbn in activeNotifs) {
            if (parseNavigationNotification(sbn)) {
                return
            }
        }
    }

    /**
     * Parses real navigation notification extras safely without inventing any data.
     * Returns true if active guidance notification was detected and state updated.
     */
    fun parseNavigationNotification(sbn: StatusBarNotification): Boolean {
        return try {
            val pkg = sbn.packageName ?: return false
            val notif = sbn.notification ?: return false

            val isNavCategory = notif.category == Notification.CATEGORY_NAVIGATION
            val isKnownNavApp = pkg == "com.google.android.apps.maps" ||
                    pkg == "com.waze" ||
                    pkg.contains("osmand") ||
                    pkg.contains("sygic") ||
                    _navigationApps.value.any { it.packageName == pkg }

            if (!isNavCategory && !isKnownNavApp) return false

            val isOngoing = (notif.flags and Notification.FLAG_ONGOING_EVENT) != 0 || sbn.isOngoing
            if (!isOngoing && !isNavCategory) return false

            val extras = notif.extras ?: return false
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()

            if (title.isNullOrBlank() && text.isNullOrBlank()) {
                return false
            }

            activeNavigatingPackage = pkg
            _navigationState.value = NavigationState.Navigating(
                destination = subText.takeIf { !it.isNullOrBlank() },
                instruction = title.takeIf { !it.isNullOrBlank() },
                distance = text.takeIf { !it.isNullOrBlank() },
                direction = null,
                sourceApp = pkg
            )
            true
        } catch (_: Throwable) {
            false
        }
    }

    companion object {
        private const val TAG = "FiestaSTNavigation"
    }
}
