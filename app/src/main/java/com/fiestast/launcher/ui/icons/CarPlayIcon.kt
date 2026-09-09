package com.fiestast.launcher.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Apple CarPlay Automotive Icon.
 *
 * Clean, recognizable Apple CarPlay emblem designed for automotive head unit displays:
 * - Rendered on a standard 24x24dp viewport.
 * - Proportioned leaf and bitten-apple silhouette.
 * - Vector paths designed to cleanly accept Compose Icon tinting (white/light silver unselected, bright red selected).
 * - Matches the visual weight, padding, and clarity of standard Material Icons (Home, NearMe, Phone, etc.).
 */
val AppleCarPlayIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "AppleCarPlay",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Leaf
        path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(15.22f, 5.06f)
            curveTo(15.82f, 4.31f, 16.24f, 3.28f, 16.12f, 2.24f)
            curveTo(15.25f, 2.28f, 14.16f, 2.84f, 13.54f, 3.58f)
            curveTo(12.99f, 4.22f, 12.51f, 5.27f, 12.65f, 6.28f)
            curveTo(13.63f, 6.36f, 14.63f, 5.79f, 15.22f, 5.06f)
            close()
        }

        // Apple Body with Bite
        path(
            fill = SolidColor(Color.White),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(17.65f, 13.12f)
            curveTo(17.68f, 15.65f, 19.86f, 16.50f, 19.96f, 16.55f)
            curveTo(19.94f, 16.63f, 19.61f, 17.77f, 18.81f, 18.95f)
            curveTo(18.12f, 19.97f, 17.39f, 21.00f, 16.27f, 21.02f)
            curveTo(15.17f, 21.04f, 14.81f, 20.36f, 13.55f, 20.36f)
            curveTo(12.29f, 20.36f, 11.89f, 21.00f, 10.84f, 21.04f)
            curveTo(9.76f, 21.08f, 8.92f, 19.93f, 8.22f, 18.92f)
            curveTo(6.80f, 16.85f, 5.72f, 13.08f, 7.18f, 10.51f)
            curveTo(7.90f, 9.24f, 9.19f, 8.43f, 10.58f, 8.41f)
            curveTo(11.64f, 8.39f, 12.65f, 9.13f, 13.30f, 9.13f)
            curveTo(13.94f, 9.13f, 15.17f, 8.23f, 16.45f, 8.37f)
            curveTo(16.99f, 8.39f, 18.51f, 8.59f, 19.49f, 10.04f)
            curveTo(19.41f, 10.09f, 17.62f, 11.14f, 17.65f, 13.12f)
            close()
        }
    }.build()
}
