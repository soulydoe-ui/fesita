package com.fiestast.launcher.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import com.fiestast.launcher.android.notifications.LauncherNotificationListener
import com.fiestast.launcher.domain.model.BluetoothDeviceInfo
import com.fiestast.launcher.domain.model.CallState
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.NavigationAppInfo
import com.fiestast.launcher.domain.model.NavigationState
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.navigation.NavRoutes
import com.fiestast.launcher.ui.components.AutomotiveButton
import com.fiestast.launcher.ui.components.AutomotiveButtonStyle
import com.fiestast.launcher.ui.components.AutomotiveCard
import com.fiestast.launcher.ui.components.BottomDock
import com.fiestast.launcher.ui.components.StatusIndicator
import com.fiestast.launcher.ui.components.TopStatusBar
import com.fiestast.launcher.ui.icons.AppleCarPlayIcon
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.DeepBlack
import com.fiestast.launcher.ui.theme.Graphite
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel

@Composable
fun PlaceholderScreen(
    route: String,
    viewModel: LauncherViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clockData by viewModel.clockData.collectAsStateWithLifecycle()
    val driverMode by viewModel.driverMode.collectAsStateWithLifecycle()
    val bluetoothStatus by viewModel.bluetoothStatus.collectAsStateWithLifecycle()
    val zlinkStatus by viewModel.zlinkStatus.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    val (title, icon) = when (route) {
        NavRoutes.NAVIGATION -> "NAVIGATION" to Icons.Default.Navigation
        NavRoutes.MUSIC -> "MUSIC PLAYER" to Icons.Default.MusicNote
        NavRoutes.PHONE -> "PHONE & DIALER" to Icons.Default.Phone
        NavRoutes.BLUETOOTH -> "BLUETOOTH CONNECTIVITY" to Icons.Default.Bluetooth
        NavRoutes.RADIO -> "FM / AM RADIO" to Icons.Default.Radio
        NavRoutes.CLIMATE -> "CLIMATE CONTROL" to Icons.Default.Air
        NavRoutes.DRIVER_MODE -> "DRIVER MODE DYNAMICS" to Icons.Default.Speed
        NavRoutes.APPS -> "APPLICATIONS" to Icons.Default.Apps
        NavRoutes.SETTINGS -> "LAUNCHER SETTINGS" to Icons.Default.Settings
        NavRoutes.ZLINK -> "ZLINK 5 (CARPLAY / AUTO)" to AppleCarPlayIcon
        else -> "AUTOMOTIVE MODULE" to Icons.Default.DirectionsCar
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        TopStatusBar(
            clockData = clockData,
            driverMode = driverMode,
            bluetoothStatus = bluetoothStatus,
            zlinkStatus = zlinkStatus
        )

        // Center section
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            AutomotiveCard(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("placeholder_card_$route")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = BrightRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = title,
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        AutomotiveButton(
                            text = "Back to Home",
                            onClick = { onNavigate(NavRoutes.HOME) },
                            style = AutomotiveButtonStyle.SECONDARY,
                            testTag = "back_home_button"
                        )
                    }

                    // Section Specific Content / Real Abstraction
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (route) {
                            NavRoutes.APPS -> AppsSection(viewModel = viewModel)
                            NavRoutes.SETTINGS -> SettingsSection(viewModel = viewModel)
                            NavRoutes.DRIVER_MODE -> DriverModeSection(
                                currentMode = driverMode,
                                onSelectMode = { viewModel.selectDriverMode(it) }
                            )
                            NavRoutes.PHONE -> PhoneSection(
                                viewModel = viewModel
                            )
                            NavRoutes.NAVIGATION -> NavigationSection(
                                viewModel = viewModel
                            )
                            NavRoutes.ZLINK -> ZLinkSection(
                                status = zlinkStatus,
                                isInstalled = viewModel.isZLinkInstalled.collectAsStateWithLifecycle().value,
                                onLaunch = { viewModel.launchZLink() }
                            )
                            NavRoutes.RADIO -> RadioSection(
                                status = viewModel.radioStatus.collectAsStateWithLifecycle().value
                            )
                            NavRoutes.CLIMATE -> ClimateSection(
                                viewModel = viewModel
                            )
                            NavRoutes.BLUETOOTH -> BluetoothSection(
                                viewModel = viewModel
                            )
                            NavRoutes.MUSIC -> MusicSection(
                                viewModel = viewModel
                            )
                            else -> GenericPlaceholderSection(title = title)
                        }
                    }

                    // Route footer indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Route: /${route.lowercase()}",
                            color = LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Fiesta ST Automotive System",
                            color = LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        BottomDock(
            currentRoute = route,
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun AppsSection(viewModel: LauncherViewModel) {
    val apps by viewModel.installedApps.collectAsStateWithLifecycle()
    if (apps.isEmpty()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No third-party apps discovered",
                color = LightGray,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            AutomotiveButton(
                text = "Scan Installed Apps",
                onClick = { viewModel.refreshApps() },
                style = AutomotiveButtonStyle.SECONDARY,
                testTag = "refresh_apps_button"
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apps) { app ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Graphite.copy(alpha = 0.5f))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { viewModel.launchApp(app.packageName) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = app.label,
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.label,
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(viewModel: LauncherViewModel) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxWidth(0.7f),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("24-Hour Time Format", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Toggle between 12-hour (AM/PM) and 24-hour head unit clock", color = LightGray, fontSize = 11.sp)
            }
            Switch(
                checked = prefs.is24HourFormat,
                onCheckedChange = { viewModel.toggle24HourFormat(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = BrightRed
                ),
                modifier = Modifier.testTag("settings_24h_switch")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Metric Units (°C / km/h)", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Display automotive telemetry in metric vs imperial", color = LightGray, fontSize = 11.sp)
            }
            Switch(
                checked = prefs.isMetricUnits,
                onCheckedChange = { viewModel.toggleMetricUnits(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureWhite,
                    checkedTrackColor = BrightRed
                ),
                modifier = Modifier.testTag("settings_metric_switch")
            )
        }

        val navApps by viewModel.navigationApps.collectAsStateWithLifecycle()
        val defaultNavApp by viewModel.defaultNavigationApp.collectAsStateWithLifecycle()
        if (navApps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Preferred Navigation App", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    val activeLabel = navApps.firstOrNull { it.isPreferred || it.packageName == defaultNavApp }?.label ?: "Auto"
                    Text("Selected: $activeLabel (${navApps.size} detected)", color = LightGray, fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    navApps.forEach { app ->
                        val isPref = app.isPreferred || app.packageName == defaultNavApp
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPref) BrightRed.copy(alpha = 0.25f) else Graphite.copy(alpha = 0.4f))
                                .border(1.dp, if (isPref) BrightRed else CardBorder, RoundedCornerShape(6.dp))
                                .clickable { viewModel.setPreferredNavigationApp(app.packageName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = app.label,
                                color = if (isPref) PureWhite else LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isPref) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        val context = LocalContext.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Notification Access", color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Grant access for media player & navigation turn-by-turn sync", color = LightGray, fontSize = 11.sp)
            }
            AutomotiveButton(
                text = "Configure",
                onClick = {
                    try {
                        val intent = LauncherNotificationListener.createNotificationListenerSettingsIntent(context)
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Throwable) {
                        try {
                            val fallback = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            fallback.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(fallback)
                        } catch (_: Throwable) {
                            // Safely ignored if device lacks notification listener settings
                        }
                    }
                },
                modifier = Modifier.padding(start = 12.dp),
                testTag = "settings_notification_access_button"
            )
        }
    }
}

@Composable
private fun DriverModeSection(
    currentMode: DriverMode,
    onSelectMode: (DriverMode) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Text(
            text = "Select Fiesta ST Launcher Vehicle Profile",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Profile affects launcher responsiveness and visual accents. (No ECU/CAN bus modification)",
            color = LightGray,
            fontSize = 12.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DriverMode.entries.forEach { mode ->
                val isSelected = currentMode == mode
                AutomotiveButton(
                    text = mode.displayName,
                    onClick = { onSelectMode(mode) },
                    style = if (isSelected) AutomotiveButtonStyle.PRIMARY else AutomotiveButtonStyle.SECONDARY,
                    modifier = Modifier.weight(1f),
                    testTag = "driver_mode_select_${mode.name.lowercase()}"
                )
            }
        }
    }
}

@Composable
private fun PhoneSection(
    viewModel: LauncherViewModel
) {
    val status by viewModel.phoneStatus.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val callInfo by viewModel.currentCallInfo.collectAsStateWithLifecycle()
    val activeContact by viewModel.activeCallContact.collectAsStateWithLifecycle()
    val isInCall by viewModel.isInCall.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()

    var enteredNumber by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("phone_section")
    ) {
        // Status & Call State Header
        when (callState) {
            CallState.RINGING -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrightRed.copy(alpha = 0.2f))
                        .border(1.dp, BrightRed, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("phone_incoming_call_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.PhoneCallback,
                            contentDescription = "Incoming Call",
                            tint = BrightRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "INCOMING CALL: ${callInfo.callerNumber ?: "Ringing on Hands-Free"}",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            CallState.ACTIVE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(androidx.compose.ui.graphics.Color(0xFF1E3A20))
                        .border(1.dp, androidx.compose.ui.graphics.Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("phone_active_call_banner"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Active Call",
                            tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "CALL IN PROGRESS: ${activeContact ?: callInfo.callerNumber ?: "Hands-Free Audio"}",
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            CallState.IDLE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusIndicator(status = status, label = "Telecom")
                    connectedDevice?.let { dev ->
                        Text(
                            text = "HFP Device: $dev",
                            color = LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Dial Pad Display
        Box(
            modifier = Modifier
                .width(320.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Graphite.copy(alpha = 0.7f))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp)
                .testTag("phone_number_display"),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = enteredNumber.ifEmpty { "Enter phone number" },
                    color = if (enteredNumber.isEmpty()) LightGray else PureWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (enteredNumber.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { enteredNumber = "" }
                                .padding(4.dp)
                                .testTag("phone_clear_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    if (enteredNumber.isNotEmpty()) {
                                        enteredNumber = enteredNumber.dropLast(1)
                                    }
                                }
                                .padding(4.dp)
                                .testTag("phone_backspace_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "Backspace",
                                tint = BrightRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Keypad Grid (3 columns, 4 rows: 1-9, *, 0, #)
        val dialKeys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.testTag("phone_dial_pad")
        ) {
            dialKeys.forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        val tag = when (key) {
                            "*" -> "phone_key_star"
                            "#" -> "phone_key_hash"
                            else -> "phone_key_$key"
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(androidx.compose.ui.graphics.Color(0xFF161922))
                                .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                .clickable { enteredNumber += key }
                                .testTag(tag),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                color = PureWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons Row: Call & Open Dialer
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutomotiveButton(
                text = if (enteredNumber.isNotEmpty()) "Call Number" else "Call",
                onClick = {
                    if (enteredNumber.isNotBlank()) {
                        viewModel.makeCall(enteredNumber)
                    } else {
                        viewModel.launchDialer()
                    }
                },
                style = AutomotiveButtonStyle.PRIMARY,
                testTag = "phone_call_button"
            )
            AutomotiveButton(
                text = "Open System Dialer",
                onClick = { viewModel.launchDialer(enteredNumber.takeIf { it.isNotBlank() }) },
                style = AutomotiveButtonStyle.SECONDARY,
                testTag = "open_dialer_button"
            )
        }
    }
}

@Composable
private fun NavigationSection(viewModel: LauncherViewModel) {
    val status by viewModel.navigationStatus.collectAsStateWithLifecycle()
    val navApps by viewModel.navigationApps.collectAsStateWithLifecycle()
    val guidance by viewModel.navigationGuidance.collectAsStateWithLifecycle()
    val defaultNavApp by viewModel.defaultNavigationApp.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        StatusIndicator(status = status, label = "Navigation")
        Text(
            text = "Automotive GPS & Navigation",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        // Real navigation state display
        when (val state = guidance) {
            is NavigationState.Navigating -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Graphite.copy(alpha = 0.6f))
                        .border(1.dp, BrightRed.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BrightRed)
                            )
                            Text(
                                text = "ACTIVE GUIDANCE",
                                color = BrightRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            if (state.sourceApp != null) {
                                Text(
                                    text = "(${state.sourceApp})",
                                    color = LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        if (!state.instruction.isNullOrBlank()) {
                            Text(
                                text = state.instruction,
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val detail = listOfNotNull(state.distance, state.destination).joinToString(" • ")
                        if (detail.isNotBlank()) {
                            Text(
                                text = detail,
                                color = LightGray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            is NavigationState.Idle -> {
                Text(
                    text = "Status: IDLE — No active navigation session",
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
            is NavigationState.Unavailable -> {
                Text(
                    text = "Status: UNAVAILABLE (${state.reason})",
                    color = LightGray,
                    fontSize = 12.sp
                )
            }
        }

        // Detected Navigation Applications
        if (navApps.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Detected Navigation Apps (tap to select preferred):",
                    color = LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    navApps.forEach { app ->
                        val isPref = app.isPreferred || app.packageName == defaultNavApp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isPref) BrightRed.copy(alpha = 0.2f) else Graphite.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (isPref) BrightRed else CardBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setPreferredNavigationApp(app.packageName) }
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = app.label,
                                        tint = if (isPref) BrightRed else PureWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = app.label,
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = if (isPref) "Preferred" else "Tap to select",
                                    color = if (isPref) BrightRed else LightGray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = "No compatible navigation applications detected on head unit.",
                color = LightGray,
                fontSize = 11.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AutomotiveButton(
                text = "Launch Maps / Navigation",
                onClick = { viewModel.launchNavigation() },
                testTag = "launch_maps_button"
            )
            AutomotiveButton(
                text = "Refresh Apps",
                onClick = { viewModel.refreshNavigationApps() },
                style = AutomotiveButtonStyle.SECONDARY,
                testTag = "refresh_nav_apps_button"
            )
        }
    }
}

@Composable
private fun ZLinkSection(
    status: ServiceStatus,
    isInstalled: Boolean,
    onLaunch: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "ZLink 5")
        Text(
            text = "CarPlay & Android Auto Link",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (isInstalled) {
                "ZLink application detected on head unit. Ready to launch."
            } else {
                "ZLink is not installed on this Android environment. Ready for head-unit package detection."
            },
            color = LightGray,
            fontSize = 12.sp
        )
        AutomotiveButton(
            text = "Launch ZLink 5",
            onClick = onLaunch,
            enabled = isInstalled,
            testTag = "launch_zlink_button"
        )
    }
}

@Composable
private fun RadioSection(status: ServiceStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "Radio Hardware")
        Text(
            text = "FM / AM Tuner Interface",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hardware state: Not connected. FM frequency is not faked.",
            color = LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ClimateSection(viewModel: LauncherViewModel) {
    val status by viewModel.climateStatus.collectAsStateWithLifecycle()
    val backend by viewModel.detectedClimateBackend.collectAsStateWithLifecycle()
    val canControl by viewModel.canControlClimateHardware.collectAsStateWithLifecycle()
    val driverTemp by viewModel.driverTempCelsius.collectAsStateWithLifecycle()
    val passengerTemp by viewModel.passengerTempCelsius.collectAsStateWithLifecycle()
    val isAcOn by viewModel.isAcOn.collectAsStateWithLifecycle()
    val isAutoOn by viewModel.isAutoOn.collectAsStateWithLifecycle()
    val isFrontDefrostOn by viewModel.isFrontDefrostOn.collectAsStateWithLifecycle()
    val isRecirculationOn by viewModel.isRecirculationOn.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("climate_section")
    ) {
        StatusIndicator(status = status, label = "Climate CAN Bus")
        Text(
            text = "Vehicle Climate Controls",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        // Backend Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141720))
                .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAN INTERFACE",
                        color = LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (backend != null) backend!!.name else "DISCONNECTED",
                        color = if (backend != null) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (backend != null) {
                        "Mechanism: ${backend!!.controlMechanism} | Controllable: ${if (canControl) "Yes" else "Read-only"}"
                    } else {
                        "Hardware state: CAN bus decoder not connected. Temperature is not faked."
                    },
                    color = LightGray,
                    fontSize = 11.sp
                )
            }
        }

        // Real Temperature Readouts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Driver zone
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141720))
                    .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "DRIVER", color = LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = driverTemp?.let { String.format(java.util.Locale.US, "%.1f°C", it) } ?: "--.-°C",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (canControl) BrightRed else Color(0xFF222632))
                                .clickable(enabled = canControl) { viewModel.adjustDriverTemperature(0.5f) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (canControl) BrightRed else Color(0xFF222632))
                                .clickable(enabled = canControl) { viewModel.adjustDriverTemperature(-0.5f) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Passenger zone
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141720))
                    .border(1.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "PASSENGER", color = LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = passengerTemp?.let { String.format(java.util.Locale.US, "%.1f°C", it) } ?: "--.-°C",
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (canControl) BrightRed else Color(0xFF222632))
                                .clickable(enabled = canControl) { viewModel.adjustPassengerTemperature(0.5f) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (canControl) BrightRed else Color(0xFF222632))
                                .clickable(enabled = canControl) { viewModel.adjustPassengerTemperature(-0.5f) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = PureWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick action buttons: A/C, AUTO, Defrost, Recirc
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ClimatePillButton(label = "A/C", active = isAcOn == true, enabled = canControl) {
                viewModel.toggleAc()
            }
            ClimatePillButton(label = "AUTO", active = isAutoOn == true, enabled = canControl) {
                viewModel.toggleAuto()
            }
            ClimatePillButton(label = "DEFROST", active = isFrontDefrostOn == true, enabled = canControl) {
                viewModel.toggleFrontDefrost()
            }
            ClimatePillButton(label = "RECIRC", active = isRecirculationOn == true, enabled = canControl) {
                viewModel.toggleRecirculation()
            }
        }
    }
}

@Composable
private fun ClimatePillButton(
    label: String,
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) BrightRed else Color(0xFF181B24))
            .border(1.dp, if (active) BrightRed else CardBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) PureWhite else (if (enabled) LightGray else LightGray.copy(alpha = 0.4f)),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BluetoothSection(viewModel: LauncherViewModel) {
    val status by viewModel.bluetoothStatus.collectAsStateWithLifecycle()
    val isEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val connectedDevice by viewModel.connectedDevice.collectAsStateWithLifecycle()
    val isA2dpConnected by viewModel.isA2dpConnected.collectAsStateWithLifecycle()
    val isHeadsetConnected by viewModel.isHeadsetConnected.collectAsStateWithLifecycle()
    val connectedDevices by viewModel.connectedBluetoothDevices.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("bluetooth_section")
    ) {
        StatusIndicator(status = status, label = "Bluetooth")
        Text(
            text = "Bluetooth Audio & Hands-Free (A2DP / HFP)",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        // Adapter Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(8.dp))
                .background(Graphite.copy(alpha = 0.5f))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hardware Adapter",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isEnabled) "ENABLED" else "DISABLED",
                        color = if (isEnabled) BrightRed else LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profile A2DP Badge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isA2dpConnected) BrightRed.copy(alpha = 0.2f) else DeepBlack)
                            .border(1.dp, if (isA2dpConnected) BrightRed else CardBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "A2DP",
                                tint = if (isA2dpConnected) BrightRed else LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isA2dpConnected) "A2DP Connected" else "A2DP Idle",
                                color = if (isA2dpConnected) PureWhite else LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Profile HFP Badge
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isHeadsetConnected) BrightRed.copy(alpha = 0.2f) else DeepBlack)
                            .border(1.dp, if (isHeadsetConnected) BrightRed else CardBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = "HFP",
                                tint = if (isHeadsetConnected) BrightRed else LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isHeadsetConnected) "HFP Connected" else "HFP Idle",
                                color = if (isHeadsetConnected) PureWhite else LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Connected Devices List
        if (connectedDevices.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Connected Devices (${connectedDevices.size})",
                    color = LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                connectedDevices.forEach { dev ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(androidx.compose.ui.graphics.Color(0xFF141720))
                            .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .testTag("bluetooth_device_item")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = "Device",
                                    tint = BrightRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = dev.name,
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (dev.isA2dpConnected) {
                                    Text(
                                        text = "Audio",
                                        color = BrightRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (dev.isHeadsetConnected) {
                                    Text(
                                        text = "Hands-Free",
                                        color = PureWhite,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                text = "No fake devices. Connect your smartphone via Android Bluetooth settings.",
                color = LightGray,
                fontSize = 12.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AutomotiveButton(
                text = "Bluetooth Settings",
                onClick = { viewModel.openBluetoothSettings() },
                testTag = "open_bluetooth_settings_button"
            )
            AutomotiveButton(
                text = "Refresh",
                onClick = { viewModel.refreshBluetoothState() },
                style = AutomotiveButtonStyle.SECONDARY,
                testTag = "refresh_bluetooth_button"
            )
        }
    }
}

@Composable
private fun MusicSection(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val status by viewModel.mediaStatus.collectAsStateWithLifecycle()
    val mediaInfo by viewModel.currentMedia.collectAsStateWithLifecycle()
    val isGranted = LauncherNotificationListener.isNotificationAccessGranted(context)

    val hasActiveMedia = status is ServiceStatus.Connected && mediaInfo != null && !mediaInfo?.title.isNullOrBlank()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        if (!isGranted) {
            StatusIndicator(
                status = ServiceStatus.Unavailable("Notification listener access required"),
                label = "Media Session Access"
            )
            Text(
                text = "Grant Notification Access to enable Android MediaSession playback controls.",
                color = LightGray,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            AutomotiveButton(
                text = "Open Notification Settings",
                onClick = {
                    try {
                        val intent = LauncherNotificationListener.createNotificationListenerSettingsIntent(context)
                        context.startActivity(intent)
                    } catch (_: Throwable) {}
                },
                style = AutomotiveButtonStyle.PRIMARY,
                testTag = "grant_notification_access_button"
            )
        } else if (hasActiveMedia && mediaInfo != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Artwork
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(com.fiestast.launcher.ui.theme.CardBackground)
                        .border(1.dp, com.fiestast.launcher.ui.theme.BrightRed, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (mediaInfo?.artworkBitmap != null) {
                        Image(
                            bitmap = mediaInfo!!.artworkBitmap!!.asImageBitmap(),
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music Art",
                            tint = PureWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mediaInfo?.artist ?: "Unknown Artist",
                        color = LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = mediaInfo?.title ?: "Track",
                        color = PureWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (!mediaInfo?.album.isNullOrBlank()) {
                        Text(
                            text = mediaInfo?.album ?: "",
                            color = LightGray.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: ${mediaInfo?.packageName ?: "Media Session"}",
                        color = com.fiestast.launcher.ui.theme.BrightRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Progress Bar
            val progressFraction = if (mediaInfo!!.durationMs > 0) {
                (mediaInfo!!.positionMs.toFloat() / mediaInfo!!.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(com.fiestast.launcher.ui.theme.CardBorder)
                ) {
                    if (progressFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(4.dp)
                                .background(com.fiestast.launcher.ui.theme.BrightRed)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(mediaInfo!!.positionMs),
                        color = LightGray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = if (mediaInfo!!.durationMs > 0) formatTimeMs(mediaInfo!!.durationMs) else "--:--",
                        color = LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(com.fiestast.launcher.ui.theme.CardBackground)
                        .clickable(enabled = mediaInfo?.canSkipPrevious ?: true) { viewModel.previousMedia() }
                        .testTag("music_prev_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(com.fiestast.launcher.ui.theme.BrightRed)
                        .clickable { viewModel.playPauseMedia() }
                        .testTag("music_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mediaInfo?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (mediaInfo?.isPlaying == true) "Pause" else "Play",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(com.fiestast.launcher.ui.theme.CardBackground)
                        .clickable(enabled = mediaInfo?.canSkipNext ?: true) { viewModel.nextMedia() }
                        .testTag("music_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            // Honest Empty State
            StatusIndicator(status = status, label = "Media Session")
            Text(
                text = "NO ACTIVE MEDIA",
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Start audio playback in any media app (Spotify, YouTube Music, Radio, etc.). The launcher will automatically detect and bind active controls.",
                color = LightGray,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            AutomotiveButton(
                text = "Refresh Sessions",
                onClick = { viewModel.refreshMediaSessions() },
                style = AutomotiveButtonStyle.SECONDARY,
                testTag = "refresh_media_sessions_button"
            )
        }
    }
}

private fun formatTimeMs(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSec = millis / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}

@Composable
private fun GenericPlaceholderSection(title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = PureWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Foundational navigation route established.",
            color = LightGray,
            fontSize = 12.sp
        )
    }
}
