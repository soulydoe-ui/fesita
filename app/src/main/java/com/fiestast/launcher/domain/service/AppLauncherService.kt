package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.AppInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface AppLauncherService {
    val status: StateFlow<ServiceStatus>
    val installedApps: StateFlow<List<AppInfo>>
    fun refreshApps()
    fun launchApp(packageName: String): Boolean
}
