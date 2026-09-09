package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.NavigationAppInfo
import com.fiestast.launcher.domain.model.NavigationState
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface NavigationService {
    val status: StateFlow<ServiceStatus>
    val defaultNavigationApp: StateFlow<String?>
    val navigationApps: StateFlow<List<NavigationAppInfo>>
    val navigationState: StateFlow<NavigationState>

    fun launchNavigation(): Boolean
    fun launchNavigationApp(): Boolean
    fun searchDestination(query: String): Boolean
    fun openNavigationApp(packageName: String): Boolean
    fun setPreferredNavigationApp(packageName: String)
    fun refreshNavigationApps()
    fun cleanup()
}
