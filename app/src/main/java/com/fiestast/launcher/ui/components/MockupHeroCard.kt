package com.fiestast.launcher.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.android.clock.ClockData
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite

/**
 * 1. LEFT CARD: Clean Standalone Clock & Branding Card
 * - Sits on the clean left side of the top row
 * - NEVER overlaps the vehicle
 * - Displays large digital time, day + date, and FIESTA ST "DRIVE YOUR STORY"
 */
@Composable
fun HeroClockCard(
    clockData: ClockData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1116))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("hero_clock_card")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Time & Date
            Column {
                Text(
                    text = clockData.timeFormatted,
                    color = PureWhite,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = clockData.dateFormatted,
                    color = LightGray.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Fiesta ST Branding & Tagline
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FIESTA",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ST",
                        color = BrightRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "DRIVE YOUR STORY",
                    color = LightGray.copy(alpha = 0.7f),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )
            }
        }
    }
}

/**
 * 2. CENTER CARD: Realistic Fiesta ST Hero Centerpiece with Driver Mode Panel
 * - Matches master mockup: dominant vehicle centerpiece with red neon tubes & wet asphalt
 * - Driver Mode panel integrated on the right side of the card with vertical NORMAL / SPORT / INDIVIDUAL buttons
 * - Unobstructed view of the Fiesta ST hot hatch
 */
@Composable
fun FiestaSTCenterpieceHeroCard(
    driverMode: DriverMode,
    onSelectMode: (DriverMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090B0E))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .testTag("mockup_hero_card")
    ) {
        // State-managed Vehicle Hero Component using the exact Fiesta ST photograph
        VehicleHeroImageWrapper(
            driverMode = driverMode,
            imageResId = com.example.R.drawable.fiesta_st_hero,
            contentEndPadding = 118.dp,
            modifier = Modifier.fillMaxSize()
        )

        // Integrated Driver Mode Panel (Right side of vehicle card)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .width(106.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xE00D0F14))
                .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .testTag("hero_driver_mode_card")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Driver Mode",
                    color = PureWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )

                // 1. NORMAL
                DriverModeVerticalButton(
                    title = "NORMAL",
                    selected = driverMode == DriverMode.NORMAL,
                    onClick = { onSelectMode(DriverMode.NORMAL) },
                    testTag = "driver_mode_normal"
                )

                // 2. SPORT
                DriverModeVerticalButton(
                    title = "SPORT",
                    selected = driverMode == DriverMode.SPORT,
                    onClick = { onSelectMode(DriverMode.SPORT) },
                    testTag = "driver_mode_sport"
                )

                // 3. INDIVIDUAL
                DriverModeVerticalButton(
                    title = "INDIVIDUAL",
                    selected = driverMode == DriverMode.INDIVIDUAL,
                    onClick = { onSelectMode(DriverMode.INDIVIDUAL) },
                    testTag = "driver_mode_individual"
                )
            }
        }
    }
}

@Composable
private fun DriverModeVerticalButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val targetBg = if (selected) {
        Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF141720), Color(0xFF0F1218)))
    }
    val borderColor = if (selected) BrightRed else CardBorder.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(targetBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) PureWhite else LightGray.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * 3. RIGHT CARD: Driver Mode Selector Card
 * - Large, tactile, easily touchable buttons
 * - NORMAL, SPORT (active glowing red), INDIVIDUAL
 */
@Composable
fun HeroDriverModeCard(
    driverMode: DriverMode,
    onSelectMode: (DriverMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1116))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("hero_driver_mode_card")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Driver Mode",
                    tint = if (driverMode == DriverMode.SPORT) BrightRed else LightGray.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DRIVER MODE",
                    color = LightGray.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // 3 Large Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DriverModeTouchButton(
                    title = "NORMAL",
                    selected = driverMode == DriverMode.NORMAL,
                    onClick = { onSelectMode(DriverMode.NORMAL) },
                    modifier = Modifier.weight(1f),
                    testTag = "driver_mode_normal"
                )
                DriverModeTouchButton(
                    title = "SPORT",
                    selected = driverMode == DriverMode.SPORT,
                    onClick = { onSelectMode(DriverMode.SPORT) },
                    modifier = Modifier.weight(1f),
                    testTag = "driver_mode_sport"
                )
                DriverModeTouchButton(
                    title = "INDIV",
                    selected = driverMode == DriverMode.INDIVIDUAL,
                    onClick = { onSelectMode(DriverMode.INDIVIDUAL) },
                    modifier = Modifier.weight(1f),
                    testTag = "driver_mode_individual"
                )
            }
        }
    }
}

@Composable
private fun DriverModeTouchButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val targetBg = if (selected) {
        Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFF881337)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF161922), Color(0xFF10131A)))
    }
    val borderColor = if (selected) BrightRed else CardBorder.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(targetBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) PureWhite else LightGray.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Legacy composite Hero card for compatibility
 */
@Composable
fun MockupHeroCard(
    clockData: ClockData,
    driverMode: DriverMode,
    onSelectMode: (DriverMode) -> Unit,
    modifier: Modifier = Modifier
) {
    FiestaSTCenterpieceHeroCard(
        driverMode = driverMode,
        onSelectMode = onSelectMode,
        modifier = modifier
    )
}
