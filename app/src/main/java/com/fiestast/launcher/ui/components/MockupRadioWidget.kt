package com.fiestast.launcher.ui.components

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite

/**
 * Radio Widget matching the master reference mockup:
 * - Segmented band toggle: [FM (red glow)] [AM (dark)]
 * - Center: [<] [Broadcast Antenna with Signal Arcs] [>]
 * - Text below:
 *   "FM HARDWARE API UNAVAILABLE" (bold white)
 *   "Connect a supported radio module" (light gray subtext)
 */
@Composable
fun MockupRadioWidget(
    status: ServiceStatus,
    currentFrequency: String?,
    onNavigateSection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFm by remember { mutableStateOf(true) }
    val isConnected = status is ServiceStatus.Connected && !currentFrequency.isNullOrBlank()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1116))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onNavigateSection() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("mockup_radio_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Segmented Band Selector (FM / AM)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141720))
                    .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(2.dp)
            ) {
                Row {
                    // FM Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isFm) {
                                    Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B)))
                                } else {
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFm) BrightRed else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { isFm = true }
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FM",
                            color = if (isFm) PureWhite else LightGray.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // AM Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (!isFm) {
                                    Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFF991B1B)))
                                } else {
                                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (!isFm) BrightRed else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { isFm = false }
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            color = if (!isFm) PureWhite else LightGray.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Center: [<] [Broadcast Antenna Icon ((|))] [>]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left chevron button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161922))
                        .border(1.dp, CardBorder.copy(alpha = 0.6f), CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Tune Down",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                // Realistic Broadcast Antenna with Signal Waves Graphic
                RadioBroadcastAntennaIcon(
                    modifier = Modifier.size(38.dp)
                )

                Spacer(modifier = Modifier.width(28.dp))

                // Right chevron button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161922))
                        .border(1.dp, CardBorder.copy(alpha = 0.6f), CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Tune Up",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Bottom Text Readout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isConnected) (currentFrequency ?: "87.5 MHz") else "FM HARDWARE API UNAVAILABLE",
                    color = PureWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isConnected) "Active FM Receiver" else "Connect a supported radio module",
                    color = LightGray.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Custom vector canvas drawing of the automotive broadcast radio antenna icon with dual wave arcs
 * exactly matching the master reference mockup.
 */
@Composable
private fun RadioBroadcastAntennaIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (size.width < 5f || size.height < 5f) return@Canvas
        try {
            val w = size.width
            val h = size.height
            val centerX = w * 0.5f
            val topY = h * 0.25f
            val bottomY = h * 0.85f

            // Central Mast
            drawLine(
                color = PureWhite,
                start = Offset(centerX, topY),
                end = Offset(centerX, bottomY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Top Antenna Node
            drawCircle(
                color = PureWhite,
                radius = 3.dp.toPx(),
                center = Offset(centerX, topY)
            )

            // Broadcast Waves - Left Arcs
            drawArc(
                color = PureWhite.copy(alpha = 0.9f),
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - 10.dp.toPx(), topY - 5.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = PureWhite.copy(alpha = 0.65f),
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - 17.dp.toPx(), topY - 10.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 28.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Broadcast Waves - Right Arcs
            drawArc(
                color = PureWhite.copy(alpha = 0.9f),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - 2.dp.toPx(), topY - 5.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(12.dp.toPx(), 18.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = PureWhite.copy(alpha = 0.65f),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - 3.dp.toPx(), topY - 10.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 28.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
        } catch (_: Throwable) {
            // Fail safely
        }
    }
}
