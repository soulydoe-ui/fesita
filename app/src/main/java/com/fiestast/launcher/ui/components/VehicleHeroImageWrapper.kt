package com.fiestast.launcher.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.LightGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Deep Black & Graphite placeholder colors to prevent rendering stalls.
 */
val PlaceholderDeepBlack = Color(0xFF080A0E)
val PlaceholderGraphite = Color(0xFF141720)
val PlaceholderGraphiteLight = Color(0xFF1B202A)

/**
 * Lifecycle and decoding state machine for the vehicle hero image asset.
 */
sealed interface VehicleHeroImageState {
    data object Loading : VehicleHeroImageState
    data class Success(val bitmap: ImageBitmap) : VehicleHeroImageState
    data class Error(val error: Throwable?) : VehicleHeroImageState
}

/**
 * State-managed wrapper for the vehicle hero component.
 *
 * Implements the exact requested hierarchy:
 * Box
 *  ├── cinematic background
 *  ├── red ambient lighting
 *  ├── original Fiesta photo
 *  ├── shadow/reflection
 *  └── UI overlays
 *
 * Driver Mode behavior:
 * - NORMAL: subtle ambient breathing pulse only.
 * - SPORT: camera scale/zoom push (1.05x) and elevated crimson lighting.
 * - INDIVIDUAL: subtle cinematic parallax movement.
 */
@Composable
fun VehicleHeroImageWrapper(
    driverMode: DriverMode,
    modifier: Modifier = Modifier,
    @DrawableRes imageResId: Int? = null,
    imageResourceName: String = "fiesta_st_hero",
    contentDescription: String = "Ford Fiesta ST Hot Hatch"
) {
    val context = LocalContext.current

    // State-managed image loading representation
    var loadState by remember(imageResId, imageResourceName) {
        mutableStateOf<VehicleHeroImageState>(VehicleHeroImageState.Loading)
    }

    // Driver mode animations
    val isSport = driverMode == DriverMode.SPORT
    val isIndividual = driverMode == DriverMode.INDIVIDUAL

    // Camera scale / zoom push: 1.05f on SPORT, 1.0f on NORMAL/INDIVIDUAL
    val cameraScale by animateFloatAsState(
        targetValue = when (driverMode) {
            DriverMode.SPORT -> 1.05f
            DriverMode.INDIVIDUAL -> 1.02f
            DriverMode.NORMAL -> 1.00f
        },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "cameraScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "vehicleHeroTransitions")

    // Ambient breathing pulse
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = if (isSport) 0.85f else 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSport) 1200 else 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    // Subtle light sweep progress
    val lightSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSport) 3000 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightSweep"
    )

    LaunchedEffect(imageResId, imageResourceName) {
        loadState = withContext(Dispatchers.IO) {
            try {
                // Check candidate names in drawable resources
                val candidateNames = listOf(
                    imageResourceName,
                    "img_fiesta_st_hero",
                    "fiesta_st_hero",
                    "fiesta_st",
                    "fiestast",
                    "car",
                    "fiesta",
                    "my_car"
                ).distinct()

                var resolvedResId = imageResId
                if (resolvedResId == null || resolvedResId == 0) {
                    for (name in candidateNames) {
                        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
                        if (id != 0) {
                            resolvedResId = id
                            break
                        }
                    }
                }

                if (resolvedResId != null && resolvedResId != 0) {
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val decoded = BitmapFactory.decodeResource(context.resources, resolvedResId, options)
                    if (decoded != null) {
                        return@withContext VehicleHeroImageState.Success(decoded.asImageBitmap())
                    }
                }

                // Check assets folder as fallback if placed in assets
                val assetNames = listOf(
                    "img_fiesta_st_hero.png",
                    "img_fiesta_st_hero.jpg",
                    "fiesta_st.png",
                    "fiesta_st.jpg",
                    "car.png",
                    "car.jpg"
                )
                for (assetName in assetNames) {
                    try {
                        context.assets.open(assetName).use { stream ->
                            val decoded = BitmapFactory.decodeStream(stream)
                            if (decoded != null) {
                                return@withContext VehicleHeroImageState.Success(decoded.asImageBitmap())
                            }
                        }
                    } catch (_: Throwable) {
                        // Continue checking next candidate
                    }
                }

                VehicleHeroImageState.Error(null)
            } catch (t: Throwable) {
                VehicleHeroImageState.Error(t)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("vehicle_hero_image_wrapper")
    ) {
        // LAYER 1: Cinematic Background (Dark industrial studio)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PlaceholderGraphite,
                            PlaceholderDeepBlack
                        )
                    )
                )
                .testTag("vehicle_hero_placeholder")
        )

        // LAYER 2: Red Ambient Lighting (Reactive to DriverMode)
        val glowColor = when (driverMode) {
            DriverMode.SPORT -> BrightRed.copy(alpha = 0.40f * ambientPulse)
            DriverMode.INDIVIDUAL -> Color(0xFFF97316).copy(alpha = 0.30f * ambientPulse)
            DriverMode.NORMAL -> Color(0xFFDC2626).copy(alpha = 0.22f * ambientPulse)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        radius = 800f
                    )
                )
        )

        // LAYER 3: Original Fiesta Photo (with driver mode camera push & scale)
        when (val state = loadState) {
            is VehicleHeroImageState.Success -> {
                Image(
                    bitmap = state.bitmap,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(cameraScale)
                        .testTag("vehicle_hero_image_success"),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )

                // LAYER 4: Shadow & Reflection Overlay (preserves original car pixels)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0x33000000),
                                    Color(0xBB080A0E)
                                )
                            )
                        )
                )
            }

            is VehicleHeroImageState.Loading,
            is VehicleHeroImageState.Error -> {
                // Keep clean studio background with ambient lighting; no text overlay, no artificial vehicle
            }
        }

        // LAYER 5: UI Overlays (subtle driver mode atmospheric sweep)
        if (isSport) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BrightRed.copy(alpha = 0.08f * ambientPulse),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 1200f * lightSweep
                        )
                    )
            )
        }
    }
}
