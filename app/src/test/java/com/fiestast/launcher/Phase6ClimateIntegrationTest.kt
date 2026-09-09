package com.fiestast.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
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
import com.fiestast.launcher.domain.model.AirflowDirection
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase6ClimateIntegrationTest {

    private lateinit var context: Context
    private lateinit var shadowApp: ShadowApplication
    private lateinit var climateService: AndroidClimateService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context as android.app.Application)
        climateService = AndroidClimateService(context)
    }

    @After
    fun tearDown() {
        climateService.cleanUp()
    }

    @Test
    fun testNoClimateBackendReportsHonestUnavailable() {
        // Without CAN decoder or Android Car API, status must be honest UNAVAILABLE
        val status = climateService.status.value
        assertTrue(status is ServiceStatus.Unavailable)
        assertEquals(AndroidClimateService.UNAVAILABLE_REASON, (status as ServiceStatus.Unavailable).reason)

        // Temperatures, fan, A/C must be null — never faked
        assertNull(climateService.driverTempCelsius.value)
        assertNull(climateService.passengerTempCelsius.value)
        assertNull(climateService.fanSpeed.value)
        assertNull(climateService.isAcOn.value)
        assertNull(climateService.isAutoOn.value)
        assertNull(climateService.isFrontDefrostOn.value)
        assertNull(climateService.isRearDefrostOn.value)
        assertNull(climateService.isRecirculationOn.value)
        assertEquals(AirflowDirection.UNKNOWN, climateService.airflowDirection.value)
        assertNull(climateService.detectedBackend.value)
        assertFalse(climateService.canControlHardware.value)
    }

    @Test
    fun testAndroidCarApiUnavailableOnStandardAndroid() {
        // On non-Automotive Android, Android Car API is unavailable or missing privileged permission
        val backend = climateService.inspectAndConnectClimateBackend()
        // Must either be null or marked not controllable if Car API lacks privileged permission
        if (backend != null) {
            assertFalse(backend.isControllable)
        } else {
            assertTrue(climateService.status.value is ServiceStatus.Unavailable)
        }
    }

    @Test
    fun testVendorBackendUnavailableByDefault() {
        // In standard environment without aftermarket CAN packages installed, backend is null
        val backend = climateService.inspectAndConnectClimateBackend()
        assertNull(backend)
        assertNull(climateService.detectedBackend.value)
        assertFalse(climateService.canControlHardware.value)
    }

    @Test
    fun testClimateStateParsingMicrontek() {
        val intent = Intent("com.microntek.canbus.report").apply {
            putExtra("temp_left", 22.5f)
            putExtra("temp_right", 21.0f)
            putExtra("fan_speed", 3)
            putExtra("ac_on", true)
            putExtra("auto_on", true)
            putExtra("front_defrost", true)
            putExtra("rear_defrost", false)
            putExtra("recirc", false)
            putExtra("airflow_mode", 1) // FACE
        }

        climateService.handleClimateBroadcast(intent)

        assertEquals(22.5f, climateService.driverTempCelsius.value ?: 0f, 0.01f)
        assertEquals(21.0f, climateService.passengerTempCelsius.value ?: 0f, 0.01f)
        assertEquals(3, climateService.fanSpeed.value)
        assertEquals(true, climateService.isAcOn.value)
        assertEquals(true, climateService.isAutoOn.value)
        assertEquals(true, climateService.isFrontDefrostOn.value)
        assertEquals(false, climateService.isRearDefrostOn.value)
        assertEquals(false, climateService.isRecirculationOn.value)
        assertEquals(AirflowDirection.FACE, climateService.airflowDirection.value)
    }

    @Test
    fun testClimateStateParsingSyu() {
        val intent = Intent("com.syu.canbus.action.HVAC").apply {
            putExtra("temp_left", 205) // 20.5°C
            putExtra("temp_right", 195) // 19.5°C
            putExtra("wind_speed", 4)
            putExtra("ac", 1)
            putExtra("auto", 1)
            putExtra("wind_front", 1)
            putExtra("wind_rear", 0)
            putExtra("cycle", 1)
            putExtra("wind_direction", 2) // FLOOR
        }

        climateService.handleClimateBroadcast(intent)

        assertEquals(20.5f, climateService.driverTempCelsius.value ?: 0f, 0.01f)
        assertEquals(19.5f, climateService.passengerTempCelsius.value ?: 0f, 0.01f)
        assertEquals(4, climateService.fanSpeed.value)
        assertEquals(true, climateService.isAcOn.value)
        assertEquals(true, climateService.isAutoOn.value)
        assertEquals(true, climateService.isFrontDefrostOn.value)
        assertEquals(false, climateService.isRearDefrostOn.value)
        assertEquals(true, climateService.isRecirculationOn.value)
        assertEquals(AirflowDirection.FLOOR, climateService.airflowDirection.value)
    }

    @Test
    fun testMalformedDataHandlingDoesNotCrashOrFabricate() {
        // Intentionally malformed intent
        val malformedIntent = Intent("com.microntek.canbus.report").apply {
            putExtra("temp_left", -999.0f) // Invalid negative temp
            putExtra("temp_right", -1.0f)
            putExtra("fan_speed", 999) // Invalid fan speed
        }

        climateService.handleClimateBroadcast(malformedIntent)

        // Invalid values must be rejected and not set as actual temperatures
        assertNull(climateService.driverTempCelsius.value)
        assertNull(climateService.passengerTempCelsius.value)
        assertNull(climateService.fanSpeed.value)
    }

    @Test
    fun testSafeControlFailureWhenNoHardwareConnected() {
        // Calling control methods without verified backend must safely return false
        val tempResult = climateService.setDriverTemperature(22.0f)
        assertFalse(tempResult)

        val adjustResult = climateService.adjustDriverTemperature(1.0f)
        assertFalse(adjustResult)

        val passResult = climateService.setPassengerTemperature(21.0f)
        assertFalse(passResult)

        val fanResult = climateService.setFanSpeed(3)
        assertFalse(fanResult)

        val acResult = climateService.toggleAc()
        assertFalse(acResult)

        val autoResult = climateService.toggleAuto()
        assertFalse(autoResult)

        val defrostResult = climateService.toggleFrontDefrost()
        assertFalse(defrostResult)

        val recircResult = climateService.toggleRecirculation()
        assertFalse(recircResult)

        // No fake data was set
        assertNull(climateService.driverTempCelsius.value)
        assertNull(climateService.fanSpeed.value)
    }

    @Test
    fun testVendorBackendDetectionWhenPackagePresent() {
        val pm = context.packageManager
        val shadowPm = shadowOf(pm)

        val pkgName = "com.microntek.controlinfo"
        val pkgInfo = PackageInfo().apply {
            packageName = pkgName
            applicationInfo = ApplicationInfo().apply {
                this.packageName = pkgName
                this.name = "Microntek CAN"
                this.enabled = true
            }
            receivers = arrayOf(
                ActivityInfo().apply {
                    this.packageName = pkgName
                    this.name = "$pkgName.CanReceiver"
                    this.exported = true
                }
            )
        }
        shadowPm.installPackage(pkgInfo)

        val backend = climateService.inspectAndConnectClimateBackend()
        assertNotNull(backend)
        assertEquals(pkgName, backend?.packageName)
        assertTrue(backend?.isControllable == true)
        assertTrue(climateService.canControlHardware.value)
        assertEquals(ServiceStatus.Connected, climateService.status.value)
    }

    @Test
    fun testLauncherViewModelClimateIntegration() {
        val prefsRepo = SharedPreferencesLauncherRepository(context)
        val vehicleModeService = AndroidVehicleModeService(prefsRepo)
        val mediaService = AndroidMediaService(context)
        val radioService = AndroidRadioService(context)
        val navService = AndroidNavigationService(context, prefsRepo)
        val btService = AndroidBluetoothService(context)
        val phoneService = AndroidPhoneService(context)
        val zlinkService = AndroidZLinkService(context)
        val appLauncherService = AndroidAppLauncherService(context)

        val viewModel = LauncherViewModel(
            preferencesRepository = prefsRepo,
            vehicleModeService = vehicleModeService,
            mediaService = mediaService,
            radioService = radioService,
            climateService = climateService,
            bluetoothService = btService,
            phoneService = phoneService,
            navigationService = navService,
            appLauncherService = appLauncherService,
            zlinkService = zlinkService
        )

        // ViewModel climate flows must match honest climateService
        assertTrue(viewModel.climateStatus.value is ServiceStatus.Unavailable)
        assertNull(viewModel.driverTempCelsius.value)
        assertNull(viewModel.passengerTempCelsius.value)
        assertNull(viewModel.fanSpeed.value)
        assertNull(viewModel.isAcOn.value)
        assertFalse(viewModel.canControlClimateHardware.value)

        // Controls fail safely without hardware
        assertFalse(viewModel.adjustDriverTemperature(1.0f))
        assertFalse(viewModel.adjustPassengerTemperature(1.0f))
        assertFalse(viewModel.toggleAc())
        assertFalse(viewModel.toggleAuto())
        assertFalse(viewModel.toggleFrontDefrost())
        assertFalse(viewModel.toggleRearDefrost())
        assertFalse(viewModel.toggleRecirculation())
        assertFalse(viewModel.cycleAirflow())

        // Cleanup
        radioService.cleanUp()
    }
}
