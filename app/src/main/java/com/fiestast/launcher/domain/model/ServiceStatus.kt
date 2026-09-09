package com.fiestast.launcher.domain.model

sealed interface ServiceStatus {
    data object Connected : ServiceStatus
    data class Available(val message: String = "Available") : ServiceStatus
    data class Unavailable(val reason: String) : ServiceStatus
    data class Demo(val message: String) : ServiceStatus
}
