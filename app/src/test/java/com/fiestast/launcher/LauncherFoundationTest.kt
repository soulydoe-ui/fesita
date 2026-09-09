package com.fiestast.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fiestast.launcher.android.apps.AndroidAppLauncherService
import com.fiestast.launcher.android.bluetooth.AndroidBluetoothService
import com.fiestast.launcher.android.climate.AndroidClimateService
import com.fiestast.launcher.android.media.AndroidMediaService
import com.fiestast.launcher.android.navigation.AndroidNavigationService
import com.fiestast.launcher.android.phone.AndroidPhoneService
import com.fiestast.launcher.android.radio.AndroidRadioService
import com.fiestast.launcher.android.vehicle.AndroidVehicleModeService
import com.fiestast.launcher.android.zlink.AndroidZLinkService
import com.fiestast.launcher.data.repository.SharedPreferencesLauncherRepository
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.navigation.NavRoutes
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LauncherFoundationTest {

    private lateinit var context: Context
    private lateinit var repository: SharedPreferencesLauncherRepository
    private lateinit var vehicleModeService: AndroidVehicleModeService
    private lateinit var mediaService: AndroidMediaService
    private lateinit var radioService: AndroidRadioService
    private lateinit var climateService: AndroidClimateService
    private lateinit var bluetoothService: AndroidBluetoothService
    private lateinit var phoneService: AndroidPhoneService
    private lateinit var navigationService: AndroidNavigationService
    private lateinit var appLauncherService: AndroidAppLauncherService
    private lateinit var zlinkService: AndroidZLinkService
    private lateinit var viewModel: LauncherViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs before test
        context.getSharedPreferences("fiesta_st_launcher_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()

        repository = SharedPreferencesLauncherRepository(context)
        vehicleModeService = AndroidVehicleModeService(repository)
        mediaService = AndroidMediaService(context)
        radioService = AndroidRadioService(context)
        climateService = AndroidClimateService(context)
        bluetoothService = AndroidBluetoothService(context)
        phoneService = AndroidPhoneService(context)
        navigationService = AndroidNavigationService(context)
        appLauncherService = AndroidAppLauncherService(context)
        zlinkService = AndroidZLinkService(context)

        viewModel = LauncherViewModel(
            preferencesRepository = repository,
            vehicleModeService = vehicleModeService,
            mediaService = mediaService,
            radioService = radioService,
            climateService = climateService,
            bluetoothService = bluetoothService,
            phoneService = phoneService,
            navigationService = navigationService,
            appLauncherService = appLauncherService,
            zlinkService = zlinkService
        )
    }

    @Test
    fun testLauncherStartupState() {
        // Startup mode defaults to NORMAL
        assertEquals(DriverMode.NORMAL, viewModel.driverMode.value)
        // Preferences default to 12h format and metric units
        assertFalse(viewModel.preferences.value.is24HourFormat)
        assertTrue(viewModel.preferences.value.isMetricUnits)
        // Verify clock data initializes with placeholder or valid date
        assertNotNull(viewModel.clockData.value)
    }

    @Test
    fun testNavigationState() {
        // Must contain all 11 required navigation sections
        val expectedRoutes = listOf(
            "home",
            "navigation",
            "music",
            "phone",
            "bluetooth",
            "radio",
            "climate",
            "driver_mode",
            "apps",
            "settings",
            "zlink"
        )
        assertEquals(11, NavRoutes.allRoutes.size)
        expectedRoutes.forEach { route ->
            assertTrue("Expected route $route missing in NavRoutes", NavRoutes.allRoutes.contains(route))
        }
    }

    @Test
    fun testDriverModeState() {
        // Normal -> Sport
        viewModel.selectDriverMode(DriverMode.SPORT)
        assertEquals(DriverMode.SPORT, viewModel.driverMode.value)
        assertEquals(DriverMode.SPORT, vehicleModeService.currentMode.value)

        // Sport -> Individual
        viewModel.selectDriverMode(DriverMode.INDIVIDUAL)
        assertEquals(DriverMode.INDIVIDUAL, viewModel.driverMode.value)
        assertEquals(DriverMode.INDIVIDUAL, vehicleModeService.currentMode.value)

        // Individual -> Normal
        viewModel.selectDriverMode(DriverMode.NORMAL)
        assertEquals(DriverMode.NORMAL, viewModel.driverMode.value)
        assertEquals(DriverMode.NORMAL, vehicleModeService.currentMode.value)
    }

    @Test
    fun testSettingsPersistence() {
        // Toggle 24h format
        viewModel.toggle24HourFormat(true)
        assertTrue(viewModel.preferences.value.is24HourFormat)

        // Toggle metric units
        viewModel.toggleMetricUnits(false)
        assertFalse(viewModel.preferences.value.isMetricUnits)

        // Select SPORT driver mode and verify persistence across fresh repository reload
        viewModel.selectDriverMode(DriverMode.SPORT)

        val freshRepository = SharedPreferencesLauncherRepository(context)
        assertTrue(freshRepository.preferences.value.is24HourFormat)
        assertFalse(freshRepository.preferences.value.isMetricUnits)
        assertEquals(DriverMode.SPORT, freshRepository.preferences.value.selectedDriverMode)
    }

    @Test
    fun testServiceFallbackBehavior() {
        // Radio: Must be Unavailable without fake FM frequency
        assertTrue(radioService.status.value is ServiceStatus.Unavailable)
        assertNull(radioService.currentFrequency.value)

        // Climate: Must be Unavailable without fake temperatures
        assertTrue(climateService.status.value is ServiceStatus.Unavailable)
        assertNull(climateService.driverTempCelsius.value)
        assertNull(climateService.passengerTempCelsius.value)
        assertNull(climateService.fanSpeed.value)

        // Media: Must be Unavailable when no Android MediaSession is active, without fake song
        assertTrue(mediaService.status.value is ServiceStatus.Unavailable)
        assertNull(mediaService.currentMedia.value)

        // Phone: Must not fake active calls
        assertFalse(phoneService.isInCall.value)
        assertNull(phoneService.activeCallContact.value)

        // ZLink: Report not installed on test device without crashing
        assertFalse(zlinkService.isInstalled.value)
        assertTrue(zlinkService.status.value is ServiceStatus.Unavailable)
    }

    @Test
    fun testVehicleHeroImageStateAndPlaceholderFallback() {
        // Verify placeholder colors are defined as Deep Black & Graphite to prevent rendering stalls
        assertEquals(androidx.compose.ui.graphics.Color(0xFF080A0E), com.fiestast.launcher.ui.components.PlaceholderDeepBlack)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF141720), com.fiestast.launcher.ui.components.PlaceholderGraphite)

        // Verify state hierarchy
        val loadingState = com.fiestast.launcher.ui.components.VehicleHeroImageState.Loading
        val errorState = com.fiestast.launcher.ui.components.VehicleHeroImageState.Error(null)
        assertTrue(loadingState is com.fiestast.launcher.ui.components.VehicleHeroImageState)
        assertTrue(errorState is com.fiestast.launcher.ui.components.VehicleHeroImageState)
    }
}
