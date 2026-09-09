package com.fiestast.launcher.domain.model

data class LauncherPreferences(
    val is24HourFormat: Boolean = false,
    val selectedDriverMode: DriverMode = DriverMode.NORMAL,
    val isMetricUnits: Boolean = true,
    val preferredNavigationPackage: String? = null
)
