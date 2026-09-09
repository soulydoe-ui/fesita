package com.fiestast.launcher.domain.model

data class NavigationAppInfo(
    val packageName: String,
    val label: String,
    val isPreferred: Boolean = false
)
