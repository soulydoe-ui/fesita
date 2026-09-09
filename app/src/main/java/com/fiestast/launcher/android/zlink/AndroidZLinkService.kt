package com.fiestast.launcher.android.zlink

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.ZLinkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidZLinkService(
    private val context: Context
) : ZLinkService {

    // Known package identifiers used by ZLink / ZLink 5 automotive CarPlay & Android Auto head units
    private val knownZLinkPackages = listOf(
        "com.zjinnova.zlink",
        "com.zjinnova.zlink5",
        "com.zlink.app",
        "com.car.zlink",
        "com.carletter.zlink",
        "com.zlink.phone"
    )

    private val _status = MutableStateFlow<ServiceStatus>(ServiceStatus.Unavailable("Searching for ZLink 5..."))
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _isInstalled = MutableStateFlow(false)
    override val isInstalled: StateFlow<Boolean> = _isInstalled.asStateFlow()

    private var resolvedPackage: String? = null

    init {
        detectZLink()
    }

    private fun detectZLink() {
        val pm = context.packageManager
        for (pkg in knownZLinkPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                resolvedPackage = pkg
                _isInstalled.value = true
                _status.value = ServiceStatus.Available("ZLink 5 application detected ($pkg)")
                return
            } catch (_: PackageManager.NameNotFoundException) {
                // Not this package, continue checking
            }
        }

        // Check if any app with 'zlink' in package or label is installed
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = pm.queryIntentActivities(launcherIntent, 0)
        val matching = activities.firstOrNull {
            it.activityInfo.packageName.contains("zlink", ignoreCase = true) ||
                it.loadLabel(pm).toString().contains("zlink", ignoreCase = true)
        }

        if (matching != null) {
            resolvedPackage = matching.activityInfo.packageName
            _isInstalled.value = true
            _status.value = ServiceStatus.Available("ZLink application detected (${matching.activityInfo.packageName})")
        } else {
            resolvedPackage = null
            _isInstalled.value = false
            _status.value = ServiceStatus.Unavailable("ZLink 5 application not installed on this head unit")
        }
    }

    override fun launchZLink(): Boolean {
        val targetPkg = resolvedPackage ?: return false
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(targetPkg)
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
