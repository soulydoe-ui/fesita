package com.fiestast.launcher

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
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
import com.fiestast.launcher.navigation.LauncherNavHost
import com.fiestast.launcher.ui.theme.FiestaSTTheme
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel

open class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LauncherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock to landscape orientation for automotive head unit display (1280x720)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Enable edge-to-edge
        enableEdgeToEdge()

        // Immersive sticky mode for automotive head unit experience
        setupImmersiveMode()

        // Initialize Services & Architecture (Clean Service Abstraction Layer)
        val prefsRepo = SharedPreferencesLauncherRepository(applicationContext)
        val vehicleModeService = AndroidVehicleModeService(prefsRepo)
        val mediaService = AndroidMediaService(applicationContext)
        val radioService = AndroidRadioService(applicationContext)
        val climateService = AndroidClimateService(applicationContext)
        val bluetoothService = AndroidBluetoothService(applicationContext)
        val phoneService = AndroidPhoneService(applicationContext)
        val navigationService = AndroidNavigationService(applicationContext)
        val appLauncherService = AndroidAppLauncherService(applicationContext)
        val zlinkService = AndroidZLinkService(applicationContext)

        viewModel = LauncherViewModel(
            preferencesRepository = prefsRepo,
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

        setContent {
            FiestaSTTheme {
                val navController = rememberNavController()
                LauncherNavHost(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setupImmersiveMode()
        }
    }

    private fun setupImmersiveMode() {
        try {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } catch (_: Throwable) {
            // Silently ignore if window decor is not ready during early lifecycle
        }
    }
}
