package com.fiestast.launcher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.domain.model.NavigationState
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite

/**
 * Navigation / Map Card for the Home screen:
 * - Premium automotive dark map background with road grid & GPS coordinate lines
 * - Real guidance state when navigating; honest empty state when idle
 * - Never invents fake turn instructions or fake ETAs
 * - Tapping launches the system navigation app (Google Maps / Waze)
 */
@Composable
fun MockupMapWidget(
    onLaunchNavigation: () -> Unit,
    navigationState: NavigationState = NavigationState.Idle,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "navPulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "navPulseScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1218))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onLaunchNavigation() }
            .testTag("mockup_map_widget")
    ) {
        // 1. Dark Vector Road Grid Canvas (Night mode automotive map aesthetic)
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width < 10f || size.height < 10f) return@Canvas
            try {
                val w = size.width
                val h = size.height

                // Subtle dark background grid
                val gridColor = Color(0xFF181D27)
                for (i in 1..8) {
                    val yPos = h * (i * 0.12f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, yPos),
                        end = Offset(w, yPos + h * 0.05f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                for (i in 1..6) {
                    val xPos = w * (i * 0.16f)
                    drawLine(
                        color = gridColor,
                        start = Offset(xPos, 0f),
                        end = Offset(xPos - w * 0.08f, h),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Arterial road lines
                val arterialColor = Color(0xFF232A38)
                drawLine(
                    color = arterialColor,
                    start = Offset(0f, h * 0.45f),
                    end = Offset(w, h * 0.52f),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = arterialColor,
                    start = Offset(w * 0.45f, 0f),
                    end = Offset(w * 0.55f, h),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Center radar pulse ring
                val center = Offset(w * 0.5f, h * 0.45f)
                val safePulseRadius = maxOf(1f, w * 0.35f * maxOf(0.05f, pulseAnim))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0091FF).copy(alpha = 0.20f * pulseAnim),
                            Color(0xFF0091FF).copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = safePulseRadius
                    ),
                    center = center,
                    radius = safePulseRadius
                )
            } catch (_: Throwable) {
                // Fail safely
            }
        }

        // 2. Top Header Pill: "NAVIGATION"
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF151922))
                    .border(1.dp, CardBorder.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Navigation Icon",
                    tint = Color(0xFF00A3FF),
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NAVIGATION",
                color = LightGray.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }

        // 3. Center State: Real Guidance if NAVIGATING, Honest Empty State if IDLE / UNAVAILABLE
        val (mainTitle, subtitle) = when (navigationState) {
            is NavigationState.Navigating -> {
                val instruction = navigationState.instruction
                val destination = navigationState.destination
                val distance = navigationState.distance
                val title = instruction ?: destination ?: "NAVIGATING"
                val sub = when {
                    instruction != null && distance != null -> "$distance • ${destination ?: "Active Route"}"
                    distance != null -> distance
                    destination != null && destination != title -> destination
                    else -> "Active Guidance"
                }
                Pair(title, sub)
            }
            is NavigationState.Unavailable -> {
                Pair("NAVIGATION READY", "Tap to open navigation")
            }
            is NavigationState.Idle -> {
                Pair("NAVIGATION READY", "Tap to open navigation")
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF0091FF).copy(alpha = 0.25f),
                                Color(0xFF0F1218)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFF0091FF).copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = mainTitle,
                    tint = Color(0xFF00A3FF),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = mainTitle,
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                color = LightGray.copy(alpha = 0.8f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        // 4. Quick Map Controls on Right Side (Zoom +, Zoom -, Layer, GPS MyLocation)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapCircleButton(onClick = { onLaunchNavigation() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = PureWhite,
                    modifier = Modifier.size(13.dp)
                )
            }
            MapCircleButton(onClick = { onLaunchNavigation() }) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = PureWhite,
                    modifier = Modifier.size(13.dp)
                )
            }
            MapCircleButton(onClick = { onLaunchNavigation() }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Map Layers",
                    tint = PureWhite,
                    modifier = Modifier.size(13.dp)
                )
            }
            MapCircleButton(onClick = { onLaunchNavigation() }) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "GPS Location",
                    tint = PureWhite,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun MapCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color(0xCC141822))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
