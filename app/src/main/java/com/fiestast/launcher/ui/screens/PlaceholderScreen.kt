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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.navigation.NavRoutes
import com.fiestast.launcher.ui.components.AutomotiveButton
import com.fiestast.launcher.ui.components.AutomotiveButtonStyle
import com.fiestast.launcher.ui.components.AutomotiveCard
import com.fiestast.launcher.ui.components.BottomDock
import com.fiestast.launcher.ui.components.StatusIndicator
import com.fiestast.launcher.ui.components.TopStatusBar
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
        NavRoutes.ZLINK -> "ZLINK 5 (CARPLAY / AUTO)" to Icons.Default.DirectionsCar
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
                                status = viewModel.phoneStatus.collectAsStateWithLifecycle().value,
                                onLaunchDialer = { viewModel.launchDialer() }
                            )
                            NavRoutes.NAVIGATION -> NavigationSection(
                                status = viewModel.navigationStatus.collectAsStateWithLifecycle().value,
                                onLaunch = { viewModel.launchNavigation() }
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
                                status = viewModel.climateStatus.collectAsStateWithLifecycle().value
                            )
                            NavRoutes.BLUETOOTH -> BluetoothSection(
                                status = bluetoothStatus
                            )
                            NavRoutes.MUSIC -> MusicSection(
                                status = viewModel.mediaStatus.collectAsStateWithLifecycle().value
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
    status: ServiceStatus,
    onLaunchDialer: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "Telecom")
        Text(
            text = "Automotive Phone & Hands-Free Integration",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No fake calls or simulated contacts. Uses system dialer.",
            color = LightGray,
            fontSize = 12.sp
        )
        AutomotiveButton(
            text = "Open System Dialer",
            onClick = onLaunchDialer,
            testTag = "open_dialer_button"
        )
    }
}

@Composable
private fun NavigationSection(
    status: ServiceStatus,
    onLaunch: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "Navigation")
        Text(
            text = "Automotive GPS & Navigation",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Launches preferred native Android navigation application.",
            color = LightGray,
            fontSize = 12.sp
        )
        AutomotiveButton(
            text = "Launch Maps / Navigation",
            onClick = onLaunch,
            testTag = "launch_maps_button"
        )
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
private fun ClimateSection(status: ServiceStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "Climate CAN Bus")
        Text(
            text = "Vehicle Climate Controls",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hardware state: CAN bus decoder not connected. Temperature is not faked.",
            color = LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun BluetoothSection(status: ServiceStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "Bluetooth")
        Text(
            text = "Bluetooth Audio & Hands-Free",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No fake connected devices. Displays real Bluetooth adapter status.",
            color = LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MusicSection(status: ServiceStatus) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusIndicator(status = status, label = "Media Session")
        Text(
            text = "Automotive Audio Player",
            color = PureWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No fake media tracks. Ready for Android MediaSession/MediaController integration.",
            color = LightGray,
            fontSize = 12.sp
        )
    }
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
