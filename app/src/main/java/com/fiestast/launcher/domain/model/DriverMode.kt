package com.fiestast.launcher.domain.model

enum class DriverMode(
    val displayName: String,
    val description: String
) {
    NORMAL(
        displayName = "Normal",
        description = "Balanced daily driving response and efficiency"
    ),
    SPORT(
        displayName = "Sport",
        description = "Sharpened throttle response and spirited dynamics"
    ),
    INDIVIDUAL(
        displayName = "Individual",
        description = "Customizable launcher vehicle profile"
    )
}
