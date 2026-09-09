package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.CallState
import com.fiestast.launcher.domain.model.PhoneCallInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface PhoneService {
    val status: StateFlow<ServiceStatus>
    val activeCallContact: StateFlow<String?>
    val isInCall: StateFlow<Boolean>
    val callState: StateFlow<CallState>
    val currentCallInfo: StateFlow<PhoneCallInfo>
    val isCallPermissionGranted: Boolean

    fun makeCall(number: String): Boolean
    fun launchDialer(number: String? = null): Boolean
    fun cleanup()
}

