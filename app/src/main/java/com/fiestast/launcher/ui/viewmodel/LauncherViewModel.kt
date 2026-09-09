package com.fiestast.launcher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiestast.launcher.android.clock.ClockData
import com.fiestast.launcher.android.clock.SystemClockProvider
import com.fiestast.launcher.data.repository.LauncherPreferencesRepository
import com.fiestast.launcher.domain.model.AppInfo
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.LauncherPreferences
import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.AppLauncherService
import com.fiestast.launcher.domain.service.BluetoothService
import com.fiestast.launcher.domain.service.ClimateService
import com.fiestast.launcher.domain.service.MediaService
import com.fiestast.launcher.domain.service.NavigationService
import com.fiestast.launcher.domain.service.PhoneService
import com.fiestast.launcher.domain.service.RadioService
import com.fiestast.launcher.domain.service.VehicleModeService
import com.fiestast.launcher.domain.service.ZLinkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(
    private val preferencesRepository: LauncherPreferencesRepository,
    private val vehicleModeService: VehicleModeService,
    private val mediaService: MediaService,
    private val radioService: RadioService,
    private val climateService: ClimateService,
    private val bluetoothService: BluetoothService,
    private val phoneService: PhoneService,
    private val navigationService: NavigationService,
    private val appLauncherService: AppLauncherService,
    private val zlinkService: ZLinkService,
    private val clockProvider: SystemClockProvider = SystemClockProvider()
) : ViewModel() {

    val preferences: StateFlow<LauncherPreferences> = preferencesRepository.preferences

    val driverMode: StateFlow<DriverMode> = vehicleModeService.currentMode
    val vehicleModeStatus: StateFlow<ServiceStatus> = vehicleModeService.status

    val mediaStatus: StateFlow<ServiceStatus> = mediaService.status
    val currentMedia: StateFlow<MediaInfo?> = mediaService.currentMedia

    val radioStatus: StateFlow<ServiceStatus> = radioService.status
    val radioFrequency: StateFlow<Double?> = radioService.currentFrequency

    val climateStatus: StateFlow<ServiceStatus> = climateService.status

    val bluetoothStatus: StateFlow<ServiceStatus> = bluetoothService.status
    val connectedDevice: StateFlow<String?> = bluetoothService.connectedDeviceName

    val phoneStatus: StateFlow<ServiceStatus> = phoneService.status
    val activeCallContact: StateFlow<String?> = phoneService.activeCallContact
    val isInCall: StateFlow<Boolean> = phoneService.isInCall

    val navigationStatus: StateFlow<ServiceStatus> = navigationService.status

    val appLauncherStatus: StateFlow<ServiceStatus> = appLauncherService.status
    val installedApps: StateFlow<List<AppInfo>> = appLauncherService.installedApps

    val zlinkStatus: StateFlow<ServiceStatus> = zlinkService.status
    val isZLinkInstalled: StateFlow<Boolean> = zlinkService.isInstalled

    val clockData: StateFlow<ClockData> = preferences
        .flatMapLatest { prefs ->
            clockProvider.observeClock(prefs.is24HourFormat)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ClockData(timeFormatted = "12:00", amPm = "PM", dateFormatted = "Fiesta ST")
        )

    fun onTimeOrTimezoneChanged() {
        clockProvider.notifyTimeOrTimezoneChanged()
    }

    fun refreshBluetoothState() {
        bluetoothService.refreshState()
    }

    fun selectDriverMode(mode: DriverMode) {
        vehicleModeService.setDriverMode(mode)
    }

    fun toggle24HourFormat(enabled: Boolean) {
        preferencesRepository.set24HourFormat(enabled)
    }

    fun toggleMetricUnits(enabled: Boolean) {
        preferencesRepository.setMetricUnits(enabled)
    }

    fun launchNavigation(): Boolean = navigationService.launchNavigationApp()

    fun launchDialer(): Boolean = phoneService.launchDialer()

    fun launchZLink(): Boolean = zlinkService.launchZLink()

    fun launchApp(packageName: String): Boolean = appLauncherService.launchApp(packageName)

    fun refreshApps() = appLauncherService.refreshApps()
}
