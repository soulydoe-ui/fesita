package com.fiestast.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AutomotiveColorScheme = darkColorScheme(
    primary = BrightRed,
    onPrimary = PureWhite,
    primaryContainer = DarkRed,
    onPrimaryContainer = PureWhite,
    secondary = LightGray,
    onSecondary = DeepBlack,
    secondaryContainer = Graphite,
    onSecondaryContainer = PureWhite,
    tertiary = BrightRed,
    onTertiary = PureWhite,
    background = DeepBlack,
    onBackground = PureWhite,
    surface = DarkCharcoal,
    onSurface = PureWhite,
    surfaceVariant = CardBackground,
    onSurfaceVariant = LightGray,
    outline = CardBorder,
    outlineVariant = DarkMuted
)

@Composable
fun FiestaSTTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AutomotiveColorScheme,
        typography = AutomotiveTypography,
        content = content
    )
}
