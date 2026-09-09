package com.fiestast.launcher

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.fiestast.launcher.android.receivers.SystemStateReceiver
import com.fiestast.launcher.android.vehicle.AndroidVehicleModeService
import com.fiestast.launcher.android.zlink.AndroidZLinkService
import com.fiestast.launcher.data.repository.SharedPreferencesLauncherRepository
import com.fiestast.launcher.navigation.LauncherNavHost
import com.fiestast.launcher.ui.theme.FiestaSTTheme
import com.fiestast.launcher.ui.viewmodel.LauncherViewModel

open class MainActivity : ComponentActivity() {

    private lateinit var viewModel: LauncherViewModel
    private var systemStateReceiver: SystemStateReceiver? = null

    // Safe permission launcher for Android 12+ (API 31+) Bluetooth Connect
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.i(TAG, "BLUETOOTH_CONNECT permission result: granted=$isGranted")
        if (::viewModel.isInitialized) {
            viewModel.refreshBluetoothState()
        }
    }

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
        val navigationService = AndroidNavigationService(applicationContext, prefsRepo)
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

        // Register dynamic system receiver for Time/Timezone and Power events
        systemStateReceiver = SystemStateReceiver(
            onTimeOrTimezoneChanged = {
                viewModel.onTimeOrTimezoneChanged()
            }
        ).also { receiver ->
            receiver.register(applicationContext)
        }

        // Check and safely request BLUETOOTH_CONNECT permission on Android 12+ (API 31+)
        checkBluetoothPermissions()

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

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshBluetoothState()
            viewModel.onTimeOrTimezoneChanged()
            viewModel.refreshMediaSessions()
            viewModel.refreshNavigationApps()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        systemStateReceiver?.unregister(applicationContext)
        systemStateReceiver = null
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnectPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasConnectPermission) {
                try {
                    bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                } catch (t: Throwable) {
                    Log.w(TAG, "Could not launch Bluetooth permission request: ${t.message}")
                }
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

    companion object {
        private const val TAG = "FiestaSTMainActivity"
    }
}
