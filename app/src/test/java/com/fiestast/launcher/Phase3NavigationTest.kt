package com.fiestast.launcher

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.fiestast.launcher.android.navigation.AndroidNavigationService
import com.fiestast.launcher.android.notifications.NotificationListenerBridge
import com.fiestast.launcher.data.repository.SharedPreferencesLauncherRepository
import com.fiestast.launcher.domain.model.NavigationAppInfo
import com.fiestast.launcher.domain.model.NavigationState
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase3NavigationTest {

    private lateinit var context: Context
    private lateinit var prefsRepo: SharedPreferencesLauncherRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefsRepo = SharedPreferencesLauncherRepository(context)
        prefsRepo.setPreferredNavigationPackage(null)
    }

    @After
    fun tearDown() {
        prefsRepo.setPreferredNavigationPackage(null)
    }

    @Test
    fun testNavigationStateDataModels() {
        val idle = NavigationState.Idle
        assertTrue(idle is NavigationState)

        val unavail = NavigationState.Unavailable("No navigation app installed")
        assertEquals("No navigation app installed", unavail.reason)

        val navigating = NavigationState.Navigating(
            destination = "Silverstone Circuit",
            instruction = "Turn right onto Copse Corner",
            distance = "500 m",
            direction = null,
            sourceApp = "com.google.android.apps.maps"
        )
        assertEquals("Silverstone Circuit", navigating.destination)
        assertEquals("Turn right onto Copse Corner", navigating.instruction)
        assertEquals("500 m", navigating.distance)
        assertNull(navigating.direction)
        assertEquals("com.google.android.apps.maps", navigating.sourceApp)
    }

    @Test
    fun testNavigationAppInfoDataModel() {
        val app = NavigationAppInfo(
            packageName = "com.google.android.apps.maps",
            label = "Google Maps",
            isPreferred = true
        )
        assertEquals("com.google.android.apps.maps", app.packageName)
        assertEquals("Google Maps", app.label)
        assertTrue(app.isPreferred)
    }

    @Test
    fun testPreferredNavigationAppPersistence() {
        assertNull(prefsRepo.preferences.value.preferredNavigationPackage)

        prefsRepo.setPreferredNavigationPackage("com.waze")
        assertEquals("com.waze", prefsRepo.preferences.value.preferredNavigationPackage)

        // Reload to verify SharedPreferences persistence
        val reloadedRepo = SharedPreferencesLauncherRepository(context)
        assertEquals("com.waze", reloadedRepo.preferences.value.preferredNavigationPackage)

        reloadedRepo.setPreferredNavigationPackage(null)
        assertNull(reloadedRepo.preferences.value.preferredNavigationPackage)
    }

    @Test
    fun testAndroidNavigationServiceNoAppsInstalled() {
        val navService = AndroidNavigationService(context, prefsRepo)
        navService.refreshNavigationApps()

        if (navService.navigationApps.value.isEmpty()) {
            assertTrue(navService.navigationState.value is NavigationState.Unavailable)
            assertTrue(navService.status.value is ServiceStatus.Unavailable)
        }
        navService.cleanup()
    }

    @Test
    fun testAndroidNavigationServiceWithMockedApp() {
        val packageManager = context.packageManager
        val shadowPm = shadowOf(packageManager)

        val mapsPackage = "com.google.android.apps.maps"
        val resolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = mapsPackage
                name = "com.google.android.maps.MapsActivity"
                applicationInfo = ApplicationInfo().apply {
                    packageName = mapsPackage
                    name = "Google Maps"
                }
            }
        }
        val geoIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="))
        shadowPm.addResolveInfoForIntent(geoIntent, resolveInfo)

        val navService = AndroidNavigationService(context, prefsRepo)
        navService.refreshNavigationApps()

        val apps = navService.navigationApps.value
        assertTrue(apps.any { it.packageName == mapsPackage })
        assertEquals(mapsPackage, navService.defaultNavigationApp.value)
        assertTrue(navService.status.value is ServiceStatus.Available)
        assertEquals(NavigationState.Idle, navService.navigationState.value)

        navService.cleanup()
    }

    @Test
    fun testPreferredAppSelectionWithMultipleApps() {
        val packageManager = context.packageManager
        val shadowPm = shadowOf(packageManager)

        val mapsResolve = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.google.android.apps.maps"
                name = "com.google.android.maps.MapsActivity"
                applicationInfo = ApplicationInfo().apply {
                    packageName = "com.google.android.apps.maps"
                }
            }
        }
        val wazeResolve = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.waze"
                name = "com.waze.MainActivity"
                applicationInfo = ApplicationInfo().apply {
                    packageName = "com.waze"
                }
            }
        }
        val geoIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q="))
        shadowPm.addResolveInfoForIntent(geoIntent, mapsResolve)
        shadowPm.addResolveInfoForIntent(geoIntent, wazeResolve)

        val navService = AndroidNavigationService(context, prefsRepo)
        navService.refreshNavigationApps()

        // Set preferred app to Waze
        navService.setPreferredNavigationApp("com.waze")
        assertEquals("com.waze", navService.defaultNavigationApp.value)
        assertEquals("com.waze", prefsRepo.preferences.value.preferredNavigationPackage)

        val wazeApp = navService.navigationApps.value.firstOrNull { it.packageName == "com.waze" }
        assertNotNull(wazeApp)
        assertTrue(wazeApp!!.isPreferred)

        navService.cleanup()
    }

    @Test
    fun testNavigationNotificationParsingAndRemoval() {
        val navService = AndroidNavigationService(context, prefsRepo)

        val notification = Notification().apply {
            category = Notification.CATEGORY_NAVIGATION
            flags = flags or Notification.FLAG_ONGOING_EVENT
            extras = Bundle().apply {
                putCharSequence(Notification.EXTRA_TITLE, "Turn left on Abbey Road")
                putCharSequence(Notification.EXTRA_TEXT, "300 m")
                putCharSequence(Notification.EXTRA_SUB_TEXT, "Destination: Ford Test Track")
            }
        }

        val sbn = StatusBarNotification(
            "com.google.android.apps.maps",
            "com.google.android.apps.maps",
            101,
            null,
            1000,
            1000,
            0,
            notification,
            Process.myUserHandle(),
            System.currentTimeMillis()
        )

        val parsed = navService.parseNavigationNotification(sbn)
        assertTrue(parsed)

        val state = navService.navigationState.value
        assertTrue(state is NavigationState.Navigating)
        val nav = state as NavigationState.Navigating
        assertEquals("Turn left on Abbey Road", nav.instruction)
        assertEquals("300 m", nav.distance)
        assertEquals("Destination: Ford Test Track", nav.destination)
        assertEquals("com.google.android.apps.maps", nav.sourceApp)

        // Test notification removal transitions back to Idle
        navService.onNotificationRemoved(sbn)
        assertTrue(navService.navigationState.value is NavigationState.Idle || navService.navigationState.value is NavigationState.Unavailable)

        navService.cleanup()
    }

    @Test
    fun testMalformedNotificationHandling() {
        val navService = AndroidNavigationService(context, prefsRepo)

        val notification = Notification().apply {
            category = Notification.CATEGORY_NAVIGATION
            extras = Bundle() // Empty extras
        }

        val sbn = StatusBarNotification(
            "com.google.android.apps.maps",
            "com.google.android.apps.maps",
            102,
            null,
            1000,
            1000,
            0,
            notification,
            Process.myUserHandle(),
            System.currentTimeMillis()
        )

        // Must not crash or transition to navigating with blank data
        val parsed = navService.parseNavigationNotification(sbn)
        assertFalse(parsed)

        navService.cleanup()
    }

    @Test
    fun testDestinationSearchIntent() {
        val navService = AndroidNavigationService(context, prefsRepo)

        val shadowContext = shadowOf(context as android.app.Application)

        val queryHandled = navService.searchDestination("Ford Performance Center")
        val startedIntents = shadowContext.nextStartedActivity
        if (queryHandled) {
            assertNotNull(startedIntents)
            assertEquals(Intent.ACTION_VIEW, startedIntents.action)
            assertTrue(startedIntents.dataString?.contains("Ford%20Performance%20Center") == true)
        }

        // Empty query should return false
        assertFalse(navService.searchDestination(""))

        navService.cleanup()
    }

    @Test
    fun testViewModelNavigationIntegration() {
        val navService = AndroidNavigationService(context, prefsRepo)
        val vehicleModeService = com.fiestast.launcher.android.vehicle.AndroidVehicleModeService(prefsRepo)
        val mediaService = com.fiestast.launcher.android.media.AndroidMediaService(context)
        val radioService = com.fiestast.launcher.android.radio.AndroidRadioService(context)
        val climateService = com.fiestast.launcher.android.climate.AndroidClimateService(context)
        val bluetoothService = com.fiestast.launcher.android.bluetooth.AndroidBluetoothService(context)
        val phoneService = com.fiestast.launcher.android.phone.AndroidPhoneService(context)
        val appLauncherService = com.fiestast.launcher.android.apps.AndroidAppLauncherService(context)
        val zlinkService = com.fiestast.launcher.android.zlink.AndroidZLinkService(context)

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

        assertNotNull(viewModel.navigationApps)
        assertNotNull(viewModel.navigationGuidance)
        assertNotNull(viewModel.defaultNavigationApp)

        viewModel.setPreferredNavigationApp("com.google.android.apps.maps")
        assertEquals("com.google.android.apps.maps", prefsRepo.preferences.value.preferredNavigationPackage)

        navService.cleanup()
    }
}
