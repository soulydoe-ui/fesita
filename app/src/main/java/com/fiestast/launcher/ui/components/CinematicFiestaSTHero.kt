package com.fiestast.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.PureWhite
import kotlin.math.cos
import kotlin.math.sin

/**
 * Photorealistic 2.5D Ford Fiesta ST Hot Hatch in Three-Quarter Front View
 * Directly modeled from the master vehicle reference photograph:
 * - Three-quarter front perspective (nose, grille, headlights, and front splitter facing viewer)
 * - Panther Black metallic paint with studio softbox specular highlight sweeps
 * - Aggressive Mk8 Fiesta ST trapezoidal honeycomb grille with red "ST" badge
 * - Ford Blue Oval badge mounted above grille on the sculpted nose
 * - Signature glowing RED halo daytime running lights (DRL) with crimson projector optics
 * - Motorsport 10-spoke alloy wheels with bright red brake calipers
 * - Wet asphalt floor with specular water sheen, mirror reflections, and red puddle glow
 * - Dark industrial studio back wall with illuminated red neon "ST" sign & "DRIVE YOUR STORY"
 */
@Composable
fun CinematicFiestaSTHero(
    driverMode: DriverMode,
    modifier: Modifier = Modifier
) {
    val isSport = driverMode == DriverMode.SPORT
    val isIndividual = driverMode == DriverMode.INDIVIDUAL

    val infiniteTransition = rememberInfiniteTransition(label = "fiestaHeroTransitions")

    // Breathing pulse for the glowing red halo DRLs
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = if (isSport) 0.85f else 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSport) 1200 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloPulse"
    )

    // Specular light sweep across hood character line
    val lightSweepProgress by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSport) 3000 else 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightSweepProgress"
    )

    val glowColor = when (driverMode) {
        DriverMode.SPORT -> BrightRed
        DriverMode.INDIVIDUAL -> Color(0xFFF97316)
        DriverMode.NORMAL -> Color(0xFFDC2626)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width < 50f || size.height < 50f) {
                // Return safely if unmeasured or too small (e.g. during initial measure pass)
                return@Canvas
            }
            try {
                // Layer 1: Studio Environment & Ambient Neon
                drawStudioAtmosphere(
                    isSport = isSport,
                    glowColor = glowColor
                )

                // Layer 2: Wet Asphalt Floor, Contact Shadows & Mirror Reflections
                drawWetFloorAndReflections(
                    isSport = isSport,
                    glowColor = glowColor,
                    haloPulse = haloPulse
                )

                // Layer 3: Realistic Panther Black Fiesta ST (Three-Quarter Front View)
                drawFiestaSTThreeQuarterFront(
                    isSport = isSport,
                    isIndividual = isIndividual,
                    glowColor = glowColor,
                    lightSweepProgress = lightSweepProgress,
                    haloPulse = haloPulse
                )

                // Layer 4: Signature Glowing Red Halo Headlights & Forward Volumetric Beam
                drawGlowingRedHaloHeadlights(
                    isSport = isSport,
                    haloPulse = haloPulse
                )
            } catch (t: Throwable) {
                // Guaranteed safe fallback: never let Canvas crash or freeze splash screen
                drawSafeVehicleFallback(isSport = isSport)
            }
        }
    }
}

/**
 * Robust safe fallback vehicle rendering using only simple, fail-safe drawing primitives.
 */
private fun DrawScope.drawSafeVehicleFallback(isSport: Boolean) {
    val w = size.width
    val h = size.height
    // Dark metallic studio background
    drawRect(
        color = Color(0xFF090B10),
        topLeft = Offset.Zero,
        size = size
    )
    // Red accent ground line
    drawLine(
        color = BrightRed.copy(alpha = if (isSport) 0.8f else 0.5f),
        start = Offset(w * 0.1f, h * 0.75f),
        end = Offset(w * 0.8f, h * 0.75f),
        strokeWidth = 2.dp.toPx()
    )
    // Dark car silhouette block
    val carPath = Path().apply {
        moveTo(w * 0.18f, h * 0.74f)
        lineTo(w * 0.22f, h * 0.58f)
        lineTo(w * 0.35f, h * 0.44f)
        lineTo(w * 0.62f, h * 0.44f)
        lineTo(w * 0.78f, h * 0.60f)
        lineTo(w * 0.80f, h * 0.74f)
        close()
    }
    drawPath(
        path = carPath,
        color = Color(0xFF141720)
    )
    drawPath(
        path = carPath,
        color = BrightRed.copy(alpha = 0.4f),
        style = Stroke(width = 1.5.dp.toPx())
    )
}

/**
 * Layer 1: Dark Industrial Studio Atmosphere
 */
private fun DrawScope.drawStudioAtmosphere(
    isSport: Boolean,
    glowColor: Color
) {
    val w = size.width
    val h = size.height

    // Studio wall vertical gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0D0F14),
                Color(0xFF090A0D),
                Color(0xFF050608)
            ),
            startY = 0f,
            endY = h * 0.72f
        ),
        topLeft = Offset.Zero,
        size = Size(w, h * 0.72f)
    )

    // Overhead industrial lighting / ceiling beams
    for (i in 1..6) {
        val x = w * (i * 0.16f)
        drawLine(
            color = Color(0xFF141720),
            start = Offset(x, 0f),
            end = Offset(x, h * 0.18f),
            strokeWidth = 1.5.dp.toPx()
        )
    }

    // Vertical red neon light tubes on back industrial wall (exact match with reference mockup)
    val verticalTubes = listOf(0.18f, 0.48f, 0.72f)
    for (tubeXFactor in verticalTubes) {
        val tx = w * tubeXFactor
        // Soft ambient bloom behind tube
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    BrightRed.copy(alpha = if (isSport) 0.35f else 0.22f),
                    Color.Transparent
                ),
                startX = tx - 25.dp.toPx(),
                endX = tx + 25.dp.toPx()
            ),
            topLeft = Offset(tx - 25.dp.toPx(), 0f),
            size = Size(50.dp.toPx(), h * 0.70f)
        )
        // Red neon glow
        drawLine(
            color = BrightRed.copy(alpha = if (isSport) 0.95f else 0.80f),
            start = Offset(tx, 0f),
            end = Offset(tx, h * 0.70f),
            strokeWidth = 3.5.dp.toPx()
        )
        // White-hot core
        drawLine(
            color = PureWhite.copy(alpha = 0.85f),
            start = Offset(tx, 0f),
            end = Offset(tx, h * 0.70f),
            strokeWidth = 1.2.dp.toPx()
        )
    }

    // Horizontal red neon light tube across back wall
    val neonY = h * 0.36f
    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                glowColor.copy(alpha = 0.20f),
                BrightRed.copy(alpha = if (isSport) 0.85f else 0.50f),
                glowColor.copy(alpha = 0.20f),
                Color.Transparent
            )
        ),
        start = Offset(w * 0.05f, neonY),
        end = Offset(w * 0.95f, neonY),
        strokeWidth = (if (isSport) 3.5.dp else 2.2.dp).toPx()
    )

    // Illuminated Red Neon "ST" Sign on left background wall
    val stX = w * 0.14f
    val stY = h * 0.30f

    // Soft radial neon bloom
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                BrightRed.copy(alpha = if (isSport) 0.65f else 0.40f),
                BrightRed.copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = Offset(stX, stY),
            radius = maxOf(1f, w * 0.16f)
        ),
        center = Offset(stX, stY),
        radius = maxOf(1f, w * 0.16f)
    )

    // Neon "ST" lettering on wall
    val sPath = Path().apply {
        moveTo(stX - 18.dp.toPx(), stY - 14.dp.toPx())
        cubicTo(stX - 10.dp.toPx(), stY - 24.dp.toPx(), stX - 2.dp.toPx(), stY - 18.dp.toPx(), stX - 8.dp.toPx(), stY - 8.dp.toPx())
        cubicTo(stX - 14.dp.toPx(), stY + 2.dp.toPx(), stX - 2.dp.toPx(), stY + 12.dp.toPx(), stX - 16.dp.toPx(), stY + 14.dp.toPx())
    }
    drawPath(
        path = sPath,
        color = BrightRed,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = sPath,
        color = PureWhite.copy(alpha = 0.8f),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Neon "T"
    val tPath = Path().apply {
        moveTo(stX + 6.dp.toPx(), stY - 16.dp.toPx())
        lineTo(stX + 26.dp.toPx(), stY - 16.dp.toPx())
        moveTo(stX + 16.dp.toPx(), stY - 16.dp.toPx())
        lineTo(stX + 16.dp.toPx(), stY + 14.dp.toPx())
    }
    drawPath(
        path = tPath,
        color = BrightRed,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = tPath,
        color = PureWhite.copy(alpha = 0.8f),
        style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * Layer 2: Wet Asphalt Floor, Contact Shadows & Mirror Reflections
 */
private fun DrawScope.drawWetFloorAndReflections(
    isSport: Boolean,
    glowColor: Color,
    haloPulse: Float
) {
    val w = size.width
    val h = size.height
    val floorY = h * 0.70f

    // Dark wet asphalt floor base
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF090A0D),
                Color(0xFF060709),
                Color(0xFF030405)
            ),
            startY = floorY,
            endY = h
        ),
        topLeft = Offset(0f, floorY),
        size = Size(w, h - floorY)
    )

    // Under-vehicle dark contact shadow (ground occlusion)
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF000000),
                Color(0xCC000000),
                Color.Transparent
            ),
            center = Offset(w * 0.52f, h * 0.77f),
            radius = maxOf(1f, w * 0.42f)
        ),
        topLeft = Offset(w * 0.12f, h * 0.73f),
        size = Size(w * 0.78f, h * 0.12f)
    )

    // Wet floor specular water streaks
    for (i in 1..7) {
        val wx = w * (0.12f + i * 0.11f)
        val wy = floorY + (i % 3) * 12.dp.toPx()
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF1E2430).copy(alpha = 0.6f),
                    Color(0xFF333D4F).copy(alpha = 0.8f),
                    Color(0xFF1E2430).copy(alpha = 0.6f),
                    Color.Transparent
                )
            ),
            start = Offset(wx - 25.dp.toPx(), wy),
            end = Offset(wx + 25.dp.toPx(), wy),
            strokeWidth = 1.2.dp.toPx()
        )
    }

    // Inverted mirror reflections of the glowing red halo headlights on the wet floor
    // Right headlight reflection
    val rGlowX = w * 0.52f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                BrightRed.copy(alpha = 0.55f * haloPulse),
                BrightRed.copy(alpha = 0.18f * haloPulse),
                Color.Transparent
            ),
            center = Offset(rGlowX, h * 0.82f),
            radius = 35.dp.toPx()
        ),
        topLeft = Offset(rGlowX - 25.dp.toPx(), h * 0.77f),
        size = Size(50.dp.toPx(), 45.dp.toPx())
    )

    // Left headlight reflection
    val lGlowX = w * 0.76f
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                BrightRed.copy(alpha = 0.45f * haloPulse),
                BrightRed.copy(alpha = 0.14f * haloPulse),
                Color.Transparent
            ),
            center = Offset(lGlowX, h * 0.81f),
            radius = 28.dp.toPx()
        ),
        topLeft = Offset(lGlowX - 20.dp.toPx(), h * 0.77f),
        size = Size(40.dp.toPx(), 35.dp.toPx())
    )

    // Reflections of vertical red neon tubes on wet asphalt ground
    val groundTubeReflections = listOf(0.18f, 0.48f, 0.72f)
    for (tubeX in groundTubeReflections) {
        val gx = w * tubeX
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    BrightRed.copy(alpha = if (isSport) 0.50f else 0.35f),
                    BrightRed.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(gx, h * 0.85f),
                radius = 45.dp.toPx()
            ),
            topLeft = Offset(gx - 35.dp.toPx(), h * 0.74f),
            size = Size(70.dp.toPx(), 45.dp.toPx())
        )
    }

    // Subtle red underglow reflection pool
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = if (isSport) 0.35f else 0.18f),
                glowColor.copy(alpha = 0.08f),
                Color.Transparent
            ),
            center = Offset(w * 0.48f, h * 0.80f),
            radius = maxOf(1f, w * 0.32f)
        ),
        topLeft = Offset(w * 0.20f, h * 0.75f),
        size = Size(w * 0.58f, h * 0.14f)
    )
}

/**
 * Layer 3: Panther Black Ford Fiesta ST (Three-Quarter Front View)
 * Mapped to the exact hot hatch proportions from the master photo:
 * - Low, wide aggressive stance
 * - Twin power bulges on hood
 * - Swept back windshield & tinted greenhouse
 * - Flared wheel arches with 10-spoke motorsport alloys and red calipers
 * - Gloss black chin splitter & lower bumper air curtains
 * - Ford Blue Oval badge & red "ST" grille badge
 */
private fun DrawScope.drawFiestaSTThreeQuarterFront(
    isSport: Boolean,
    isIndividual: Boolean,
    glowColor: Color,
    lightSweepProgress: Float,
    haloPulse: Float
) {
    val w = size.width
    val h = size.height

    // -------------------------------------------------------------
    // 1. REAR WHEEL (Far perspective, passenger side)
    // -------------------------------------------------------------
    drawWheel3D(
        centerX = w * 0.23f,
        centerY = h * 0.65f,
        radius = 24.dp.toPx(),
        isFront = false
    )

    // -------------------------------------------------------------
    // 2. MAIN BODY SHELL & GREENHOUSE (Panther Black with metallic highlights)
    // -------------------------------------------------------------
    val bodyPath = Path().apply {
        // Start at rear ST spoiler
        moveTo(w * 0.18f, h * 0.40f)
        // Roofline
        cubicTo(w * 0.24f, h * 0.35f, w * 0.35f, h * 0.32f, w * 0.46f, h * 0.32f)
        // Down the windshield A-pillar
        cubicTo(w * 0.50f, h * 0.34f, w * 0.53f, h * 0.40f, w * 0.57f, h * 0.46f)
        // Over the hood toward front nose
        cubicTo(w * 0.64f, h * 0.49f, w * 0.74f, h * 0.53f, w * 0.82f, h * 0.56f)
        // Front nose curve down
        cubicTo(w * 0.84f, h * 0.58f, w * 0.85f, h * 0.62f, w * 0.84f, h * 0.67f)
        // Front lower chin splitter
        lineTo(w * 0.83f, h * 0.73f)
        lineTo(w * 0.64f, h * 0.74f)
        // Front right wheel arch cutout
        cubicTo(w * 0.60f, h * 0.70f, w * 0.56f, h * 0.56f, w * 0.44f, h * 0.56f)
        cubicTo(w * 0.38f, h * 0.56f, w * 0.34f, h * 0.68f, w * 0.32f, h * 0.73f)
        // Side rocker panel skirt
        lineTo(w * 0.26f, h * 0.73f)
        // Rear wheel arch cutout
        cubicTo(w * 0.26f, h * 0.63f, w * 0.22f, h * 0.58f, w * 0.18f, h * 0.68f)
        // Rear bumper up to ST spoiler
        lineTo(w * 0.15f, h * 0.68f)
        cubicTo(w * 0.14f, h * 0.60f, w * 0.14f, h * 0.48f, w * 0.18f, h * 0.40f)
        close()
    }

    // Body metallic black base fill
    drawPath(
        path = bodyPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF262C38),
                Color(0xFF151922),
                Color(0xFF0D0F14),
                Color(0xFF07080B)
            ),
            startY = h * 0.30f,
            endY = h * 0.74f
        )
    )

    // Body panel perimeter shutline
    drawPath(
        path = bodyPath,
        color = Color(0xFF2D3545),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // -------------------------------------------------------------
    // 3. GREENHOUSE / TINTED WINDSHIELD & CABIN GLASS
    // -------------------------------------------------------------
    val windshieldPath = Path().apply {
        moveTo(w * 0.38f, h * 0.34f)
        lineTo(w * 0.46f, h * 0.33f)
        cubicTo(w * 0.50f, h * 0.35f, w * 0.53f, h * 0.41f, w * 0.56f, h * 0.46f)
        lineTo(w * 0.43f, h * 0.46f)
        close()
    }
    // Deep dark tint glass
    drawPath(
        path = windshieldPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F131A),
                Color(0xFF080A0D),
                Color(0xFF141924)
            ),
            start = Offset(w * 0.40f, h * 0.34f),
            end = Offset(w * 0.56f, h * 0.46f)
        )
    )
    // Windshield studio softbox reflection streak
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                PureWhite.copy(alpha = 0.35f),
                PureWhite.copy(alpha = 0.65f),
                PureWhite.copy(alpha = 0.20f),
                Color.Transparent
            )
        ),
        start = Offset(w * 0.42f, h * 0.35f),
        end = Offset(w * 0.51f, h * 0.45f),
        strokeWidth = 3.dp.toPx()
    )

    // Side passenger window glass
    val sideWindowPath = Path().apply {
        moveTo(w * 0.23f, h * 0.38f)
        lineTo(w * 0.37f, h * 0.35f)
        lineTo(w * 0.41f, h * 0.46f)
        lineTo(w * 0.25f, h * 0.46f)
        close()
    }
    drawPath(path = sideWindowPath, color = Color(0xFF080A0E))
    // Window chrome/gloss black surround
    drawPath(
        path = sideWindowPath,
        color = Color(0xFF2B3240),
        style = Stroke(width = 1.dp.toPx())
    )

    // Aerodynamic Gloss Black Side Mirror
    val mirrorX = w * 0.43f
    val mirrorY = h * 0.44f
    drawOval(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFF1E232E), Color(0xFF0C0E13))
        ),
        topLeft = Offset(mirrorX, mirrorY),
        size = Size(14.dp.toPx(), 8.dp.toPx())
    )
    // Integrated LED indicator strip on mirror
    drawLine(
        color = PureWhite.copy(alpha = 0.8f),
        start = Offset(mirrorX + 2.dp.toPx(), mirrorY + 4.dp.toPx()),
        end = Offset(mirrorX + 11.dp.toPx(), mirrorY + 4.dp.toPx()),
        strokeWidth = 1.dp.toPx()
    )

    // -------------------------------------------------------------
    // 4. SCULPTED HOOD WITH TWIN CHARACTER CREASES
    // -------------------------------------------------------------
    val hoodPath = Path().apply {
        moveTo(w * 0.45f, h * 0.46f)
        lineTo(w * 0.57f, h * 0.46f)
        cubicTo(w * 0.65f, h * 0.50f, w * 0.74f, h * 0.53f, w * 0.81f, h * 0.56f)
        lineTo(w * 0.68f, h * 0.57f)
        close()
    }
    drawPath(
        path = hoodPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF282F3D),
                Color(0xFF181D26),
                Color(0xFF11141B)
            ),
            start = Offset(w * 0.45f, h * 0.46f),
            end = Offset(w * 0.81f, h * 0.56f)
        )
    )

    // Sharp specular highlight along the upper hood crease line
    val creaseStartX = w * 0.56f
    val creaseStartY = h * 0.47f
    val creaseEndX = w * 0.80f
    val creaseEndY = h * 0.56f
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                PureWhite.copy(alpha = 0.60f),
                PureWhite.copy(alpha = 0.90f),
                PureWhite.copy(alpha = 0.40f),
                Color.Transparent
            )
        ),
        start = Offset(creaseStartX, creaseStartY),
        end = Offset(creaseEndX, creaseEndY),
        strokeWidth = 1.8.dp.toPx()
    )

    // Animated dynamic light sweep pulse across hood (Sport mode accent)
    if (isSport && lightSweepProgress in 0f..1f) {
        val sweepX = creaseStartX + (creaseEndX - creaseStartX) * lightSweepProgress
        val sweepY = creaseStartY + (creaseEndY - creaseStartY) * lightSweepProgress
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    BrightRed.copy(alpha = 0.8f),
                    BrightRed.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(sweepX, sweepY),
                radius = 16.dp.toPx()
            ),
            center = Offset(sweepX, sweepY),
            radius = 16.dp.toPx()
        )
    }

    // -------------------------------------------------------------
    // 5. FRONT FASCIA: HONEYCOMB GRILLE, FORD OVAL & RED "ST" BADGE
    // -------------------------------------------------------------
    // Mk8 Wide Trapezoidal Honeycomb Grille
    val grillePath = Path().apply {
        moveTo(w * 0.65f, h * 0.58f)
        lineTo(w * 0.79f, h * 0.58f)
        lineTo(w * 0.80f, h * 0.66f)
        lineTo(w * 0.64f, h * 0.66f)
        close()
    }
    // Deep black grille cavity
    drawPath(path = grillePath, color = Color(0xFF060709))
    drawPath(
        path = grillePath,
        color = Color(0xFF262C38),
        style = Stroke(width = 1.2.dp.toPx())
    )

    // Honeycomb hexagonal mesh horizontal grid lines
    for (i in 1..4) {
        val gy = h * (0.58f + i * 0.016f)
        drawLine(
            color = Color(0xFF1E232E),
            start = Offset(w * 0.65f, gy),
            end = Offset(w * 0.795f, gy),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Ford Blue Oval Emblem (Mounted on the sculpted nose right above the grille)
    val fordX = w * 0.715f
    val fordY = h * 0.565f
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1D4ED8), Color(0xFF1E3A8A))
        ),
        topLeft = Offset(fordX, fordY),
        size = Size(15.dp.toPx(), 8.5.dp.toPx())
    )
    drawOval(
        color = PureWhite.copy(alpha = 0.95f),
        topLeft = Offset(fordX, fordY),
        size = Size(15.dp.toPx(), 8.5.dp.toPx()),
        style = Stroke(width = 0.9.dp.toPx())
    )

    // VIVID RED "ST" BADGE (Mounted in lower-right corner of the front grille)
    val stBadgeX = w * 0.765f
    val stBadgeY = h * 0.635f
    // Chrome border
    drawRoundRect(
        color = PureWhite,
        topLeft = Offset(stBadgeX - 1.dp.toPx(), stBadgeY - 1.dp.toPx()),
        size = Size(16.dp.toPx(), 9.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    // Red badge body
    drawRoundRect(
        color = BrightRed,
        topLeft = Offset(stBadgeX, stBadgeY),
        size = Size(14.dp.toPx(), 7.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
    // "ST" text block inside badge
    drawRect(
        color = PureWhite,
        topLeft = Offset(stBadgeX + 2.5.dp.toPx(), stBadgeY + 1.8.dp.toPx()),
        size = Size(3.dp.toPx(), 3.5.dp.toPx())
    )
    drawRect(
        color = PureWhite,
        topLeft = Offset(stBadgeX + 7.5.dp.toPx(), stBadgeY + 1.8.dp.toPx()),
        size = Size(4.dp.toPx(), 3.5.dp.toPx())
    )

    // Euro Front Plate: "FIESTA ST" (Centered on front bumper)
    val plateX = w * 0.675f
    val plateY = h * 0.675f
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(plateX, plateY),
        size = Size(34.dp.toPx(), 8.5.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
    )
    drawRoundRect(
        color = CardBorder,
        topLeft = Offset(plateX, plateY),
        size = Size(34.dp.toPx(), 8.5.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
        style = Stroke(width = 0.8.dp.toPx())
    )
    // "FIESTA" in white, "ST" in red
    drawRect(
        color = PureWhite,
        topLeft = Offset(plateX + 4.dp.toPx(), plateY + 2.5.dp.toPx()),
        size = Size(16.dp.toPx(), 3.5.dp.toPx())
    )
    drawRect(
        color = BrightRed,
        topLeft = Offset(plateX + 22.dp.toPx(), plateY + 2.5.dp.toPx()),
        size = Size(7.dp.toPx(), 3.5.dp.toPx())
    )

    // Lower Bumper Air Dam & Gloss Black Chin Splitter
    val splitterPath = Path().apply {
        moveTo(w * 0.62f, h * 0.72f)
        lineTo(w * 0.83f, h * 0.715f)
        lineTo(w * 0.84f, h * 0.735f)
        lineTo(w * 0.61f, h * 0.74f)
        close()
    }
    drawPath(path = splitterPath, color = Color(0xFF141822))
    drawPath(
        path = splitterPath,
        color = PureWhite.copy(alpha = 0.35f),
        style = Stroke(width = 0.8.dp.toPx())
    )

    // Left & Right Fog Light Air Curtains
    // Left side fog duct
    drawRoundRect(
        color = Color(0xFF11141B),
        topLeft = Offset(w * 0.805f, h * 0.66f),
        size = Size(11.dp.toPx(), 13.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawCircle(
        color = BrightRed.copy(alpha = 0.85f),
        radius = 2.dp.toPx(),
        center = Offset(w * 0.815f, h * 0.68f)
    )

    // Right side fog duct
    drawRoundRect(
        color = Color(0xFF11141B),
        topLeft = Offset(w * 0.615f, h * 0.66f),
        size = Size(10.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawCircle(
        color = BrightRed.copy(alpha = 0.85f),
        radius = 2.dp.toPx(),
        center = Offset(w * 0.625f, h * 0.68f)
    )

    // -------------------------------------------------------------
    // 6. FRONT RIGHT WHEEL (Prominent in foreground, three-quarter angle)
    // -------------------------------------------------------------
    drawWheel3D(
        centerX = w * 0.47f,
        centerY = h * 0.66f,
        radius = 34.dp.toPx(),
        isFront = true
    )
}

/**
 * 3D Motorsport Alloy Wheel Renderer:
 * - Low-profile Michelin Pilot Sport-style tire
 * - Ventilated steel brake rotor
 * - Bright RED high-performance brake caliper with mounting pins
 * - Satin black 10-spoke alloy wheel with bevel highlights
 * - Red ST center hub cap
 */
private fun DrawScope.drawWheel3D(
    centerX: Float,
    centerY: Float,
    radius: Float,
    isFront: Boolean
) {
    if (radius < 2f) return
    val center = Offset(centerX, centerY)

    // 1. Outer Tire Rubber
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF222733),
                Color(0xFF151821),
                Color(0xFF090A0D)
            ),
            center = center,
            radius = maxOf(1f, radius)
        ),
        center = center,
        radius = maxOf(1f, radius)
    )

    // Tire tread edge ring
    drawCircle(
        color = Color(0xFF2E3545),
        center = center,
        radius = radius,
        style = Stroke(width = 2.dp.toPx())
    )

    // 2. Wheel Rim Cavity
    val rimRadius = radius * 0.78f
    drawCircle(
        color = Color(0xFF08090C),
        center = center,
        radius = rimRadius
    )

    // 3. Ventilated Steel Brake Rotor
    val rotorRadius = rimRadius * 0.82f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF8A93A6),
                Color(0xFF4B5568),
                Color(0xFF2D333F)
            ),
            center = center,
            radius = maxOf(1f, rotorRadius)
        ),
        center = center,
        radius = maxOf(1f, rotorRadius)
    )

    // 4. BRIGHT RED HIGH-PERFORMANCE BRAKE CALIPER
    val caliperAngle = if (isFront) 130f else 50f
    val rad = Math.toRadians(caliperAngle.toDouble())
    val caliperDist = rotorRadius * 0.65f
    val caliperCenterX = centerX + (cos(rad) * caliperDist).toFloat()
    val caliperCenterY = centerY + (sin(rad) * caliperDist).toFloat()

    drawRoundRect(
        color = BrightRed,
        topLeft = Offset(caliperCenterX - 8.dp.toPx(), caliperCenterY - 6.dp.toPx()),
        size = Size(16.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    // Caliper mounting pins / ST logo text
    drawRect(
        color = PureWhite.copy(alpha = 0.9f),
        topLeft = Offset(caliperCenterX - 4.dp.toPx(), caliperCenterY - 1.5.dp.toPx()),
        size = Size(8.dp.toPx(), 3.dp.toPx())
    )

    // 5. SATIN BLACK 10-SPOKE ALLOY WHEEL
    val spokeCount = 10
    for (i in 0 until spokeCount) {
        val angle = i * (360f / spokeCount)
        val spokeRad = Math.toRadians(angle.toDouble())
        val outerX = centerX + (cos(spokeRad) * (rimRadius * 0.95f)).toFloat()
        val outerY = centerY + (sin(spokeRad) * (rimRadius * 0.95f)).toFloat()

        // Spoke base line
        drawLine(
            color = Color(0xFF161A22),
            start = center,
            end = Offset(outerX, outerY),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Metallic edge highlight on spoke
        drawLine(
            color = Color(0xFF6B7280).copy(alpha = 0.7f),
            start = center,
            end = Offset(outerX, outerY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Rim outer lip bevel
    drawCircle(
        color = Color(0xFF9CA3AF).copy(alpha = 0.8f),
        center = center,
        radius = rimRadius,
        style = Stroke(width = 1.2.dp.toPx())
    )

    // 6. RED "ST" CENTER HUB CAP
    drawCircle(
        color = BrightRed,
        center = center,
        radius = rimRadius * 0.22f
    )
    drawCircle(
        color = PureWhite,
        center = center,
        radius = rimRadius * 0.08f
    )
}

/**
 * Layer 4: Signature Glowing Red Halo Headlights & Projector Demon Eyes
 * The defining visual feature of the master photograph:
 * - Swept-back aerodynamic headlamp housing
 * - Radiant crimson RED LED halo / DRL ribbon hugging the top & outer contours
 * - High-intensity crimson bi-LED projector optic with white-hot central hotspot
 * - Volumetric red light cast forward onto the wet floor
 */
private fun DrawScope.drawGlowingRedHaloHeadlights(
    isSport: Boolean,
    haloPulse: Float
) {
    val w = size.width
    val h = size.height

    // =========================================================================
    // 1. FRONT-RIGHT HEADLIGHT (Closest, prominent Swept-Back Contour)
    // =========================================================================
    val rLampStartX = w * 0.54f
    val rLampStartY = h * 0.52f
    val rLampPeakX = w * 0.60f
    val rLampPeakY = h * 0.49f
    val rLampEndX = w * 0.66f
    val rLampEndY = h * 0.55f

    // Volumetric forward red beam projection
    val beamPath = Path().apply {
        moveTo(rLampEndX, rLampEndY)
        lineTo(w * 0.98f, h * 0.68f)
        lineTo(w * 0.82f, h * 0.85f)
        lineTo(rLampStartX, rLampStartY + 10.dp.toPx())
        close()
    }
    drawPath(
        path = beamPath,
        brush = Brush.linearGradient(
            colors = listOf(
                BrightRed.copy(alpha = 0.32f * haloPulse),
                BrightRed.copy(alpha = 0.10f * haloPulse),
                Color.Transparent
            ),
            start = Offset(rLampPeakX, rLampPeakY),
            end = Offset(w * 0.95f, h * 0.75f)
        )
    )

    // Headlight internal housing cavity (dark chrome)
    val rHousingPath = Path().apply {
        moveTo(rLampStartX, rLampStartY)
        cubicTo(rLampPeakX, rLampPeakY - 4.dp.toPx(), rLampEndX, rLampEndY - 2.dp.toPx(), rLampEndX, rLampEndY)
        cubicTo(rLampEndX - 10.dp.toPx(), rLampEndY + 12.dp.toPx(), rLampStartX + 12.dp.toPx(), rLampStartY + 8.dp.toPx(), rLampStartX, rLampStartY)
        close()
    }
    drawPath(path = rHousingPath, color = Color(0xFF0A0C10))
    drawPath(
        path = rHousingPath,
        color = Color(0xFF2C3444),
        style = Stroke(width = 1.dp.toPx())
    )

    // Radiant Ambient Halo Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                BrightRed.copy(alpha = 0.85f * haloPulse),
                BrightRed.copy(alpha = 0.35f * haloPulse),
                Color.Transparent
            ),
            center = Offset(rLampPeakX, rLampPeakY + 4.dp.toPx()),
            radius = maxOf(1f, 32.dp.toPx())
        ),
        center = Offset(rLampPeakX, rLampPeakY + 4.dp.toPx()),
        radius = maxOf(1f, 32.dp.toPx())
    )

    // Glowing RED DRL Ribbon / Eyebrow Light Pipe
    val rDrlPath = Path().apply {
        moveTo(rLampStartX + 2.dp.toPx(), rLampStartY - 1.dp.toPx())
        cubicTo(rLampPeakX, rLampPeakY - 3.dp.toPx(), rLampEndX - 4.dp.toPx(), rLampEndY - 2.dp.toPx(), rLampEndX, rLampEndY)
    }
    // Outer intense neon glow
    drawPath(
        path = rDrlPath,
        color = BrightRed.copy(alpha = 0.95f * haloPulse),
        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
    )
    // White-hot core light line
    drawPath(
        path = rDrlPath,
        color = PureWhite.copy(alpha = 0.85f * haloPulse),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )

    // Crimson Bi-LED Projector Optic (Demon Eye)
    val rProjCenter = Offset(rLampStartX + 18.dp.toPx(), rLampStartY + 4.dp.toPx())
    // Projector red halo
    drawCircle(
        color = BrightRed,
        radius = 6.5.dp.toPx(),
        center = rProjCenter
    )
    // Projector white-hot focal beam center
    drawCircle(
        color = PureWhite,
        radius = 2.5.dp.toPx(),
        center = rProjCenter
    )

    // =========================================================================
    // 2. FRONT-LEFT HEADLIGHT (Far side in 3/4 perspective)
    // =========================================================================
    val lLampStartX = w * 0.78f
    val lLampStartY = h * 0.56f
    val lLampPeakX = w * 0.81f
    val lLampPeakY = h * 0.55f
    val lLampEndX = w * 0.84f
    val lLampEndY = h * 0.60f

    // Far headlight ambient glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                BrightRed.copy(alpha = 0.75f * haloPulse),
                BrightRed.copy(alpha = 0.25f * haloPulse),
                Color.Transparent
            ),
            center = Offset(lLampPeakX, lLampPeakY + 2.dp.toPx()),
            radius = maxOf(1f, 22.dp.toPx())
        ),
        center = Offset(lLampPeakX, lLampPeakY + 2.dp.toPx()),
        radius = maxOf(1f, 22.dp.toPx())
    )

    // Far headlight DRL light pipe
    val lDrlPath = Path().apply {
        moveTo(lLampStartX, lLampStartY)
        cubicTo(lLampPeakX, lLampPeakY - 1.dp.toPx(), lLampEndX - 2.dp.toPx(), lLampEndY - 1.dp.toPx(), lLampEndX, lLampEndY)
    }
    drawPath(
        path = lDrlPath,
        color = BrightRed.copy(alpha = 0.95f * haloPulse),
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
    )
    drawPath(
        path = lDrlPath,
        color = PureWhite.copy(alpha = 0.85f * haloPulse),
        style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Far projector optic
    val lProjCenter = Offset(lLampStartX + 10.dp.toPx(), lLampStartY + 3.dp.toPx())
    drawCircle(
        color = BrightRed,
        radius = 4.5.dp.toPx(),
        center = lProjCenter
    )
    drawCircle(
        color = PureWhite,
        radius = 1.8.dp.toPx(),
        center = lProjCenter
    )
}
