package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface PhoneService {
    val status: StateFlow<ServiceStatus>
    val activeCallContact: StateFlow<String?>
    val isInCall: StateFlow<Boolean>
    fun launchDialer(): Boolean
}
