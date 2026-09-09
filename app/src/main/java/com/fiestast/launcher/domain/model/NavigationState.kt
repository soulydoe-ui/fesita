package com.fiestast.launcher.domain.model

sealed class NavigationState {
    object Idle : NavigationState()

    data class Navigating(
        val destination: String? = null,
        val instruction: String? = null,
        val distance: String? = null,
        val direction: String? = null,
        val sourceApp: String? = null
    ) : NavigationState()

    data class Unavailable(
        val reason: String = "No navigation application installed"
    ) : NavigationState()
}
