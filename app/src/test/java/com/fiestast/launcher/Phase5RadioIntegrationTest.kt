package com.fiestast.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
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
class Phase5RadioIntegrationTest {

    private lateinit var context: Context
    private lateinit var shadowApp: ShadowApplication
    private lateinit var radioService: AndroidRadioService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context as android.app.Application)
        radioService = AndroidRadioService(context)
    }

    @After
    fun tearDown() {
        radioService.cleanUp()
    }

    @Test
    fun testNoRadioBackendReportsHonestUnavailable() {
        // Without radio hardware or OEM radio package, state must be honest UNAVAILABLE
        val status = radioService.status.value
        assertTrue(status is ServiceStatus.Unavailable)
        assertEquals(AndroidRadioService.UNAVAILABLE_REASON, (status as ServiceStatus.Unavailable).reason)

        // Frequency MUST NOT be faked
        assertNull(radioService.currentFrequency.value)
        assertNull(radioService.detectedRadioPackage.value)
        assertFalse(radioService.canControlHardware.value)
    }

    @Test
    fun testTuningDoesNotInventFakeFrequencyWhenUnavailable() {
        // Tuning or seeking must NOT fabricate frequency numbers when hardware is absent
        radioService.tune(99.9)
        assertNull(radioService.currentFrequency.value)

        radioService.seekNext()
        assertNull(radioService.currentFrequency.value)

        radioService.seekPrevious()
        assertNull(radioService.currentFrequency.value)

        // Launch fails gracefully without crashing
        assertFalse(radioService.launchRadioApp())
    }

    @Test
    fun testDetectsInstalledMicrontekRadioPackage() {
        // Mock installation of a head-unit radio package with exported components
        val pkgName = "com.microntek.radio"
        val radioActivityInfo = ActivityInfo().apply {
            packageName = pkgName
            name = "com.microntek.radio.RadioActivity"
            exported = true
            applicationInfo = ApplicationInfo().apply {
                packageName = pkgName
                nonLocalizedLabel = "Car Radio"
            }
        }
        val radioReceiverInfo = ActivityInfo().apply {
            packageName = pkgName
            name = "com.microntek.radio.RadioReceiver"
            exported = true
            applicationInfo = ApplicationInfo().apply {
                packageName = pkgName
            }
        }

        val pkgInfo = PackageInfo().apply {
            packageName = pkgName
            applicationInfo = ApplicationInfo().apply {
                packageName = pkgName
                name = "Car Radio"
                nonLocalizedLabel = "Car Radio"
            }
            activities = arrayOf(radioActivityInfo)
            receivers = arrayOf(radioReceiverInfo)
        }

        val pm = shadowOf(context.packageManager)
        pm.installPackage(pkgInfo)

        // Add launch intent resolve info
        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(pkgName, "com.microntek.radio.RadioActivity")
        }
        pm.addResolveInfoForIntent(
            launchIntent,
            ResolveInfo().apply {
                activityInfo = radioActivityInfo
            }
        )

        // Re-inspect backend
        val freshRadioService = AndroidRadioService(context)
        try {
            val backend = freshRadioService.inspectAndConnectRadioBackend()
            assertNotNull(backend)
            assertEquals(pkgName, backend?.packageName)
            assertTrue(backend?.exportedActivities?.contains("com.microntek.radio.RadioActivity") == true)
            assertTrue(backend?.exportedReceivers?.contains("com.microntek.radio.RadioReceiver") == true)

            // Status should be Available with backend info
            assertTrue(freshRadioService.status.value is ServiceStatus.Available)
            assertEquals(pkgName, freshRadioService.detectedRadioPackage.value)
            assertTrue(freshRadioService.canControlHardware.value)
            assertEquals("Microntek Intent Broadcast", backend?.controlMechanism)

            // Frequency should still be null until actual broadcast from hardware
            assertNull(freshRadioService.currentFrequency.value)
        } finally {
            freshRadioService.cleanUp()
        }
    }

    @Test
    fun testRadioBroadcastUpdatesRealFrequency() {
        // Simulate hardware reporting frequency via Microntek broadcast
        val intent = Intent("com.microntek.radio.report").apply {
            putExtra("freq", 10110) // 101.10 MHz
        }
        radioService.broadcastReceiver.onReceive(context, intent)

        assertEquals(101.10, radioService.currentFrequency.value ?: 0.0, 0.01)

        // Simulate frequency change via Syu broadcast
        val syuIntent = Intent("com.syu.ms.action.RADIO").apply {
            putExtra("freq", 9850) // 98.50 MHz
        }
        radioService.broadcastReceiver.onReceive(context, syuIntent)

        assertEquals(98.50, radioService.currentFrequency.value ?: 0.0, 0.01)
    }

    @Test
    fun testLauncherViewModelRadioIntegration() {
        val prefsRepo = SharedPreferencesLauncherRepository(context)
        val vehicleModeService = AndroidVehicleModeService(prefsRepo)
        val mediaService = AndroidMediaService(context)
        val climateService = AndroidClimateService(context)
        val bluetoothService = AndroidBluetoothService(context)
        val phoneService = AndroidPhoneService(context)
        val navService = AndroidNavigationService(context, prefsRepo)
        val appLauncherService = AndroidAppLauncherService(context)
        val zlinkService = AndroidZLinkService(context)

        val viewModel = LauncherViewModel(
            preferencesRepository = prefsRepo,
            vehicleModeService = vehicleModeService,
            mediaService = mediaService,
            radioService = radioService,
            climateService = climateService,
            bluetoothService = bluetoothService,
            phoneService = phoneService,
            navigationService = navService,
            appLauncherService = appLauncherService,
            zlinkService = zlinkService
        )

        // Honest status without hardware
        assertTrue(viewModel.radioStatus.value is ServiceStatus.Unavailable)
        assertEquals(AndroidRadioService.UNAVAILABLE_REASON, (viewModel.radioStatus.value as ServiceStatus.Unavailable).reason)
        assertNull(viewModel.radioFrequency.value)
        assertNull(viewModel.detectedRadioPackage.value)
        assertFalse(viewModel.canControlRadioHardware.value)

        // Actions do not crash or fake data
        viewModel.tuneRadio(105.5)
        assertNull(viewModel.radioFrequency.value)
        assertFalse(viewModel.launchRadioApp())
    }
}
