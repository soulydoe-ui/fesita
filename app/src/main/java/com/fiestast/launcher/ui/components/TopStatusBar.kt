package com.fiestast.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.android.clock.ClockData
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.DarkCharcoal
import com.fiestast.launcher.ui.theme.DeepBlack
import com.fiestast.launcher.ui.theme.Graphite
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite
import com.fiestast.launcher.ui.theme.StatusGreen

@Composable
fun TopStatusBar(
    clockData: ClockData,
    driverMode: DriverMode,
    bluetoothStatus: ServiceStatus,
    zlinkStatus: ServiceStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(DarkCharcoal.copy(alpha = 0.95f))
            .border(width = 1.dp, color = CardBorder.copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Fiesta ST Branding & Driver Mode Pill
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "FIESTA" in sleek white tracking
            Text(
                text = "FIESTA",
                color = PureWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            // Red ST Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BrightRed)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ST",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Driver Mode Badge
            val modeColor = when (driverMode) {
                DriverMode.NORMAL -> LightGray
                DriverMode.SPORT -> BrightRed
                DriverMode.INDIVIDUAL -> Color(0xFFF59E0B)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, modeColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .background(Graphite.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = driverMode.name,
                    color = modeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Center: Head Unit Telemetry Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val zlinkAvailable = zlinkStatus is ServiceStatus.Available || zlinkStatus is ServiceStatus.Connected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (zlinkAvailable) StatusGreen.copy(alpha = 0.2f) else DeepBlack)
                    .border(1.dp, if (zlinkAvailable) StatusGreen else CardBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "ZLINK 5",
                    color = if (zlinkAvailable) PureWhite else LightGray.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val btAvailable = bluetoothStatus is ServiceStatus.Available || bluetoothStatus is ServiceStatus.Connected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (btAvailable) Graphite else DeepBlack)
                    .border(1.dp, if (btAvailable) LightGray.copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "BT",
                    color = if (btAvailable) PureWhite else LightGray.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Right: Real System Clock (Automotive Digits)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = clockData.dateFormatted,
                    color = LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = clockData.timeFormatted,
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    if (clockData.amPm.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = clockData.amPm,
                            color = BrightRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
