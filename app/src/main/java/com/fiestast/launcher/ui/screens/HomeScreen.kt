package com.fiestast.launcher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fiestast.launcher.navigation.NavRoutes
import com.fiestast.launcher.ui.components.BottomDock
import com.fiestast.launcher.ui.components.FiestaSTCenterpieceHeroCard
import com.fiestast.launcher.ui.components.HeroClockCard
import com.fiestast.launcher.ui.components.MockupClimateWidget
import com.fiestast.launcher.ui.components.MockupMapWidget
import com.fiestast.launcher.ui.components.MockupMusicWidget
import com.fiestast.launcher.ui.components.MockupRadioWidget
import com.fiestast.launcher.ui.components.MockupTopBar
import com.fiestast.launcher.ui.theme.DeepBlack
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel

/**
 * Home screen strictly following the exact reference visual hierarchy:
 *
 * TOP BAR:
 * Home pill + FIESTA ST logo, status icons (Bluetooth, Wi-Fi, Cellular, Clock)
 *
 * FIRST ROW (1280x720 Automotive Landscape Hero Area):
 * - LEFT: Large digital clock + date + FIESTA ST "DRIVE YOUR STORY" (clean area, no car overlap)
 * - CENTER: Large realistic Panther Black Ford Fiesta ST in 3/4 front view (dominant centerpiece)
 * - RIGHT: Navigation (empty ready state) + Driver Mode (large touch buttons: NORMAL, SPORT, INDIVIDUAL)
 *
 * SECOND ROW:
 * - Music Widget (authentic MediaSession data or "NO ACTIVE MEDIA")
 * - Climate Widget (uncompressed layout, automotive icons, "--.-°C" with "CLIMATE API UNAVAILABLE")
 * - Radio Widget (FM/AM toggle, authentic frequency or "FM HARDWARE API UNAVAILABLE")
 *
 * BOTTOM DOCK:
 * Home | Navigation | Music | Phone | Radio | Apps | Settings | ZLink
 */
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clockData by viewModel.clockData.collectAsStateWithLifecycle()
    val driverMode by viewModel.driverMode.collectAsStateWithLifecycle()
    val mediaStatus by viewModel.mediaStatus.collectAsStateWithLifecycle()
    val currentMedia by viewModel.currentMedia.collectAsStateWithLifecycle()
    val climateStatus by viewModel.climateStatus.collectAsStateWithLifecycle()
    val driverTemp by viewModel.driverTempCelsius.collectAsStateWithLifecycle()
    val passengerTemp by viewModel.passengerTempCelsius.collectAsStateWithLifecycle()
    val fanSpeed by viewModel.fanSpeed.collectAsStateWithLifecycle()
    val isAcOn by viewModel.isAcOn.collectAsStateWithLifecycle()
    val isAutoOn by viewModel.isAutoOn.collectAsStateWithLifecycle()
    val isFrontDefrostOn by viewModel.isFrontDefrostOn.collectAsStateWithLifecycle()
    val isRearDefrostOn by viewModel.isRearDefrostOn.collectAsStateWithLifecycle()
    val isRecirculationOn by viewModel.isRecirculationOn.collectAsStateWithLifecycle()
    val radioStatus by viewModel.radioStatus.collectAsStateWithLifecycle()
    val radioFrequency by viewModel.radioFrequency.collectAsStateWithLifecycle()
    val navigationGuidance by viewModel.navigationGuidance.collectAsStateWithLifecycle()

    val radioFreqString = radioFrequency?.let { String.format(java.util.Locale.US, "%.1f", it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // 1. Top Status Bar
        MockupTopBar(
            clockData = clockData,
            onHomeClick = { /* Already on Home */ }
        )

        // 2. Main Automotive Dashboard (Landscape 1280x720)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            // TOP ROW: Hero Area (Clock on Left | Fiesta ST Centerpiece with Driver Mode | Navigation on Right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.48f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEFT: Clean Standalone Clock & Branding Card (Zero overlap with car)
                HeroClockCard(
                    clockData = clockData,
                    modifier = Modifier
                        .weight(0.72f)
                        .fillMaxHeight()
                )

                // CENTER: Large Realistic Panther Black Fiesta ST Centerpiece with Integrated Driver Mode Panel
                FiestaSTCenterpieceHeroCard(
                    driverMode = driverMode,
                    onSelectMode = { mode -> viewModel.selectDriverMode(mode) },
                    modifier = Modifier
                        .weight(2.05f)
                        .fillMaxHeight()
                )

                // RIGHT: Navigation Card (Spans full height of top row matching master mockup)
                MockupMapWidget(
                    onLaunchNavigation = {
                        viewModel.launchNavigation()
                        onNavigate(NavRoutes.NAVIGATION)
                    },
                    navigationState = navigationGuidance,
                    modifier = Modifier
                        .weight(1.0f)
                        .fillMaxHeight()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SECOND ROW: Music | Climate | Radio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Music Widget
                MockupMusicWidget(
                    status = mediaStatus,
                    mediaInfo = currentMedia,
                    onNavigateSection = { onNavigate(NavRoutes.MUSIC) },
                    onPlayPause = { viewModel.playPauseMedia() },
                    onPrevious = { viewModel.previousMedia() },
                    onNext = { viewModel.nextMedia() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Climate Widget
                MockupClimateWidget(
                    status = climateStatus,
                    driverTemp = driverTemp,
                    passengerTemp = passengerTemp,
                    fanSpeed = fanSpeed,
                    isAcOn = isAcOn,
                    isAutoOn = isAutoOn,
                    isFrontDefrostOn = isFrontDefrostOn,
                    isRearDefrostOn = isRearDefrostOn,
                    isRecirculationOn = isRecirculationOn,
                    onDriverTempUp = { viewModel.adjustDriverTemperature(0.5f) },
                    onDriverTempDown = { viewModel.adjustDriverTemperature(-0.5f) },
                    onPassengerTempUp = { viewModel.adjustPassengerTemperature(0.5f) },
                    onPassengerTempDown = { viewModel.adjustPassengerTemperature(-0.5f) },
                    onToggleAc = { viewModel.toggleAc() },
                    onToggleAuto = { viewModel.toggleAuto() },
                    onCycleFan = { viewModel.adjustFanSpeed(1) },
                    onToggleDefrost = { viewModel.toggleFrontDefrost() },
                    onToggleRecirc = { viewModel.toggleRecirculation() },
                    onNavigateSection = { onNavigate(NavRoutes.CLIMATE) },
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                )

                // Radio Widget
                MockupRadioWidget(
                    status = radioStatus,
                    currentFrequency = radioFreqString,
                    onNavigateSection = { onNavigate(NavRoutes.RADIO) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }

        // 3. Bottom Dock Bar (8 items)
        BottomDock(
            currentRoute = NavRoutes.HOME,
            onNavigate = onNavigate
        )
    }
}
