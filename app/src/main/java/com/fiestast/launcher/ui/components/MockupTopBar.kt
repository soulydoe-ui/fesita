package com.fiestast.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.android.clock.ClockData
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.PureWhite

/**
 * Top Status Bar matching the reference mockup:
 * - Far Left: Home icon button in dark pill
 * - Left brand: "FIESTA" (white bold italic) + "ST" (bright red bold italic)
 * - Far Right: Bluetooth icon, Wi-Fi icon, Cellular signal icon, Digital Clock
 */
@Composable
fun MockupTopBar(
    clockData: ClockData,
    onHomeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF07080B))
            .border(width = 1.dp, color = CardBorder.copy(alpha = 0.35f))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Home Icon Pill & FIESTA ST Brand
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Pill Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF14171E))
                    .border(1.dp, CardBorder.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .clickable { onHomeClick() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("top_bar_home_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = PureWhite,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Brand: FIESTA ST
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FIESTA ",
                    color = PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "ST",
                    color = BrightRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 1.sp
                )
            }
        }

        // Right: Status Icons & Digital Clock
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bluetooth Icon
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = "Bluetooth",
                tint = PureWhite.copy(alpha = 0.9f),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Wi-Fi Icon
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi",
                tint = PureWhite.copy(alpha = 0.9f),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Cellular Signal Icon
            Icon(
                imageVector = Icons.Default.NetworkCell,
                contentDescription = "Cellular Signal",
                tint = PureWhite.copy(alpha = 0.9f),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))

            // Digital Time
            Text(
                text = clockData.timeFormatted,
                color = PureWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.testTag("top_bar_clock")
            )
        }
    }
}
