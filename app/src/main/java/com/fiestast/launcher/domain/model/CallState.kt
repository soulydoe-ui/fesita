package com.fiestast.launcher.domain.model

enum class CallState {
    IDLE,
    RINGING,
    ACTIVE
}

data class PhoneCallInfo(
    val state: CallState = CallState.IDLE,
    val callerNumber: String? = null,
    val callerName: String? = null,
    val isRinging: Boolean = state == CallState.RINGING,
    val isActive: Boolean = state == CallState.ACTIVE
)
