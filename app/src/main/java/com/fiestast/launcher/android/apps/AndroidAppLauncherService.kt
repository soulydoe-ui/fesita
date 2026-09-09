package com.fiestast.launcher.android.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.fiestast.launcher.domain.model.AppInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.AppLauncherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidAppLauncherService(
    private val context: Context
) : AppLauncherService {

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Available("App discovery service ready")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    override val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    init {
        refreshApps()
    }

    override fun refreshApps() {
        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            val currentPkg = context.packageName

            val apps = resolveInfos
                .filter { it.activityInfo.packageName != currentPkg }
                .map { info ->
                    AppInfo(
                        packageName = info.activityInfo.packageName,
                        label = info.loadLabel(pm).toString(),
                        icon = info.loadIcon(pm)
                    )
                }
                .sortedBy { it.label.lowercase() }

            _installedApps.value = apps
            _status.value = ServiceStatus.Available("${apps.size} installed apps detected")
        } catch (e: Exception) {
            _status.value = ServiceStatus.Unavailable("Failed to query apps: ${e.message}")
        }
    }

    override fun launchApp(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
