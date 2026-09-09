package com.fiestast.launcher.domain.model

data class RadioBackendInfo(
    val packageName: String,
    val appName: String,
    val exportedActivities: List<String> = emptyList(),
    val exportedServices: List<String> = emptyList(),
    val exportedReceivers: List<String> = emptyList(),
    val isControllable: Boolean = false,
    val controlMechanism: String = "None"
)
