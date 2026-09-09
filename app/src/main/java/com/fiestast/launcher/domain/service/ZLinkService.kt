package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface ZLinkService {
    val status: StateFlow<ServiceStatus>
    val isInstalled: StateFlow<Boolean>
    fun launchZLink(): Boolean
}
