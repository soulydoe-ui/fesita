package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface NavigationService {
    val status: StateFlow<ServiceStatus>
    val defaultNavigationApp: StateFlow<String?>
    fun launchNavigationApp(): Boolean
}
