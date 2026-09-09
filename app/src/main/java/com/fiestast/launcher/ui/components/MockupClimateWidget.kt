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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite

/**
 * Automotive Climate Control Widget matching the master reference mockup:
 * - Driver zone: [+] button, [--.-° Driver], [-] button
 * - Center: Recaro cockpit interior graphic with red ambient mood glow
 * - Passenger zone: [--.-° Passenger], [+] button, [-] button
 * - Control buttons row: A/C, AUTO, Fan level (with 4 segment bars), Air Dir, Defrost, Recirc
 * - Warning pill at bottom: [! CLIMATE API UNAVAILABLE]
 */
@Composable
fun MockupClimateWidget(
    status: ServiceStatus,
    onNavigateSection: () -> Unit,
    modifier: Modifier = Modifier,
    driverTemp: Float? = null,
    passengerTemp: Float? = null,
    fanSpeed: Int? = null,
    isAcOn: Boolean? = null,
    isAutoOn: Boolean? = null,
    isFrontDefrostOn: Boolean? = null,
    isRearDefrostOn: Boolean? = null,
    isRecirculationOn: Boolean? = null,
    onDriverTempUp: () -> Unit = {},
    onDriverTempDown: () -> Unit = {},
    onPassengerTempUp: () -> Unit = {},
    onPassengerTempDown: () -> Unit = {},
    onToggleAc: () -> Unit = {},
    onToggleAuto: () -> Unit = {},
    onCycleFan: () -> Unit = {},
    onToggleDefrost: () -> Unit = {},
    onToggleRecirc: () -> Unit = {}
) {
    val isConnected = status is ServiceStatus.Connected
    var localAcActive by remember { mutableStateOf(false) }
    var localAutoActive by remember { mutableStateOf(false) }
    var localDefrostActive by remember { mutableStateOf(false) }
    var localRecircActive by remember { mutableStateOf(false) }
    var localFanLevel by remember { mutableIntStateOf(0) }

    val acActive = isAcOn ?: localAcActive
    val autoActive = isAutoOn ?: localAutoActive
    val defrostActive = isFrontDefrostOn ?: localDefrostActive
    val recircActive = isRecirculationOn ?: localRecircActive
    val effectiveFanLevel = fanSpeed ?: localFanLevel

    val driverTempText = driverTemp?.let { String.format(java.util.Locale.US, "%.1f°", it) } ?: "--.-°"
    val passengerTempText = passengerTemp?.let { String.format(java.util.Locale.US, "%.1f°", it) } ?: "--.-°"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1116))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onNavigateSection() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("mockup_climate_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Section: Driver Controls | Cockpit Interior Graphic | Passenger Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Driver side: [+] and [-] buttons + Temperature
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column with [+] and [-]
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ClimateSquareButton(icon = Icons.Default.Add, onClick = onDriverTempUp)
                        ClimateSquareButton(icon = Icons.Default.Remove, onClick = onDriverTempDown)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Temperature readout
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = driverTempText,
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Driver",
                            color = LightGray.copy(alpha = 0.75f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Center: Sleek Recaro Cabin Cockpit with Ambient Red Glow
                FiestaRecaroCockpitView(
                    modifier = Modifier
                        .weight(1f)
                        .height(65.dp)
                        .padding(horizontal = 4.dp)
                )

                // Passenger side: Temperature + [+] and [-] buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = passengerTempText,
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Passenger",
                            color = LightGray.copy(alpha = 0.75f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Column with [+] and [-]
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ClimateSquareButton(icon = Icons.Default.Add, onClick = onPassengerTempUp)
                        ClimateSquareButton(icon = Icons.Default.Remove, onClick = onPassengerTempDown)
                    }
                }
            }

            // 2. Center Buttons Row: A/C | AUTO | Fan | Air Dir | Defrost | Recirc
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClimateActionPill(
                    label = "A/C",
                    active = acActive,
                    onClick = {
                        localAcActive = !localAcActive
                        onToggleAc()
                    }
                )
                ClimateActionPill(
                    label = "AUTO",
                    active = autoActive,
                    onClick = {
                        localAutoActive = !localAutoActive
                        onToggleAuto()
                    }
                )

                // Fan Level Pill with icon + 4 segmented level bars
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161922))
                        .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable {
                            localFanLevel = (localFanLevel + 1) % 5
                            onCycleFan()
                        }
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Toys,
                            contentDescription = "Fan",
                            tint = PureWhite,
                            modifier = Modifier.size(13.dp)
                        )
                        for (i in 1..4) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (i <= effectiveFanLevel) BrightRed else Color(0xFF282D3B)
                                    )
                            )
                        }
                    }
                }

                ClimateActionIconPill(
                    icon = Icons.Default.Air,
                    label = "Air Dir",
                    active = false,
                    onClick = {}
                )
                ClimateActionIconPill(
                    icon = Icons.Default.Waves,
                    label = "Defrost",
                    active = defrostActive,
                    onClick = {
                        localDefrostActive = !localDefrostActive
                        onToggleDefrost()
                    }
                )
                ClimateActionIconPill(
                    icon = Icons.Default.DirectionsCar,
                    label = "Recirc",
                    active = recircActive,
                    onClick = {
                        localRecircActive = !localRecircActive
                        onToggleRecirc()
                    }
                )
            }

            // 3. Bottom Status Pill: Honest status representation
            val isConnectedState = status is ServiceStatus.Connected
            val isAvailableState = status is ServiceStatus.Available
            val badgeBg = if (isConnectedState) Color(0xFF101B14) else Color(0xFF1B1812)
            val badgeBorder = if (isConnectedState) Color(0xFF10B981).copy(alpha = 0.65f) else Color(0xFFD97706).copy(alpha = 0.65f)
            val badgeTint = if (isConnectedState) Color(0xFF10B981) else Color(0xFFF59E0B)
            val badgeIcon = if (isConnectedState) Icons.Default.CheckCircle else Icons.Default.WarningAmber
            val badgeText = when {
                isConnectedState -> "CAN BUS CLIMATE ACTIVE"
                isAvailableState -> status.message.uppercase(java.util.Locale.US)
                else -> "CLIMATE API UNAVAILABLE"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(badgeBg)
                    .border(1.dp, badgeBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = badgeText,
                        tint = badgeTint,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        color = badgeTint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Top-down forward perspective representation of the Fiesta ST Recaro sport interior cabin
 * with illuminated red mood lighting.
 */
@Composable
private fun FiestaRecaroCockpitView(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (size.width < 10f || size.height < 10f) return@Canvas
        try {
            val w = size.width
            val h = size.height

            // Dark cabin cockpit enclosure
            drawRoundRect(
                color = Color(0xFF0A0C10),
                size = Size(w, h),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Center console illuminated red strip
            val consoleX = w * 0.5f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BrightRed.copy(alpha = 0.7f),
                        BrightRed.copy(alpha = 0.3f),
                        Color(0xFF161922)
                    )
                ),
                topLeft = Offset(consoleX - 4.dp.toPx(), h * 0.2f),
                size = Size(8.dp.toPx(), h * 0.75f)
            )

            // Driver Left Recaro Seat
            val seatWidth = w * 0.28f
            val seatHeight = h * 0.78f
            val driverSeatX = w * 0.16f
            val seatY = h * 0.16f

            // Driver Seat Cushion & Bolsters
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E222D), Color(0xFF12141A))
                ),
                topLeft = Offset(driverSeatX, seatY),
                size = Size(seatWidth, seatHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Red bolster ambient highlight
            drawRoundRect(
                color = BrightRed.copy(alpha = 0.4f),
                topLeft = Offset(driverSeatX + 2.dp.toPx(), seatY + 4.dp.toPx()),
                size = Size(3.dp.toPx(), seatHeight - 8.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Passenger Right Recaro Seat
            val passSeatX = w * 0.56f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E222D), Color(0xFF12141A))
                ),
                topLeft = Offset(passSeatX, seatY),
                size = Size(seatWidth, seatHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Red bolster ambient highlight
            drawRoundRect(
                color = BrightRed.copy(alpha = 0.4f),
                topLeft = Offset(passSeatX + seatWidth - 5.dp.toPx(), seatY + 4.dp.toPx()),
                size = Size(3.dp.toPx(), seatHeight - 8.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Soft red ambient mood glow in footwells
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrightRed.copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(consoleX, h * 0.75f),
                    radius = maxOf(1f, w * 0.35f)
                ),
                center = Offset(consoleX, h * 0.75f),
                radius = maxOf(1f, w * 0.35f)
            )
        } catch (_: Throwable) {
            // Fail safely
        }
    }
}

@Composable
private fun ClimateSquareButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF161922))
            .border(1.dp, CardBorder.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PureWhite,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun ClimateActionPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) {
                    Brush.verticalGradient(listOf(Color(0xFFB91C1C), Color(0xFF7F1D1D)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF161922), Color(0xFF11141A)))
                }
            )
            .border(
                1.dp,
                if (active) BrightRed.copy(alpha = 0.8f) else CardBorder.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) PureWhite else LightGray.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ClimateActionIconPill(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) {
                    Brush.verticalGradient(listOf(Color(0xFFB91C1C), Color(0xFF7F1D1D)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF161922), Color(0xFF11141A)))
                }
            )
            .border(
                1.dp,
                if (active) BrightRed.copy(alpha = 0.8f) else CardBorder.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) PureWhite else LightGray.copy(alpha = 0.85f),
            modifier = Modifier.size(14.dp)
        )
    }
}
