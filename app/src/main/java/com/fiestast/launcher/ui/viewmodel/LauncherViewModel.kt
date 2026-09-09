package com.fiestast.launcher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fiestast.launcher.android.clock.ClockData
import com.fiestast.launcher.android.clock.SystemClockProvider
import com.fiestast.launcher.data.repository.LauncherPreferencesRepository
import com.fiestast.launcher.domain.model.AirflowDirection
import com.fiestast.launcher.domain.model.AppInfo
import com.fiestast.launcher.domain.model.BluetoothDeviceInfo
import com.fiestast.launcher.domain.model.CallState
import com.fiestast.launcher.domain.model.ClimateBackendInfo
import com.fiestast.launcher.domain.model.ClimateState
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.LauncherPreferences
import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.NavigationAppInfo
import com.fiestast.launcher.domain.model.NavigationState
import com.fiestast.launcher.domain.model.PhoneCallInfo
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
    val detectedRadioPackage: StateFlow<String?> = radioService.detectedRadioPackage
    val canControlRadioHardware: StateFlow<Boolean> = radioService.canControlHardware

    val climateStatus: StateFlow<ServiceStatus> = climateService.status
    val climateState: StateFlow<ClimateState> = climateService.climateState
    val driverTempCelsius: StateFlow<Float?> = climateService.driverTempCelsius
    val passengerTempCelsius: StateFlow<Float?> = climateService.passengerTempCelsius
    val fanSpeed: StateFlow<Int?> = climateService.fanSpeed
    val isAcOn: StateFlow<Boolean?> = climateService.isAcOn
    val isAutoOn: StateFlow<Boolean?> = climateService.isAutoOn
    val isFrontDefrostOn: StateFlow<Boolean?> = climateService.isFrontDefrostOn
    val isRearDefrostOn: StateFlow<Boolean?> = climateService.isRearDefrostOn
    val isRecirculationOn: StateFlow<Boolean?> = climateService.isRecirculationOn
    val airflowDirection: StateFlow<AirflowDirection> = climateService.airflowDirection
    val detectedClimateBackend: StateFlow<ClimateBackendInfo?> = climateService.detectedBackend
    val canControlClimateHardware: StateFlow<Boolean> = climateService.canControlHardware

    val bluetoothStatus: StateFlow<ServiceStatus> = bluetoothService.status
    val isBluetoothEnabled: StateFlow<Boolean> = bluetoothService.isBluetoothEnabled
    val connectedDevice: StateFlow<String?> = bluetoothService.connectedDeviceName
    val isA2dpConnected: StateFlow<Boolean> = bluetoothService.isA2dpConnected
    val isHeadsetConnected: StateFlow<Boolean> = bluetoothService.isHeadsetConnected
    val connectedBluetoothDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothService.connectedDevices

    val phoneStatus: StateFlow<ServiceStatus> = phoneService.status
    val activeCallContact: StateFlow<String?> = phoneService.activeCallContact
    val isInCall: StateFlow<Boolean> = phoneService.isInCall
    val callState: StateFlow<CallState> = phoneService.callState
    val currentCallInfo: StateFlow<PhoneCallInfo> = phoneService.currentCallInfo

    val navigationStatus: StateFlow<ServiceStatus> = navigationService.status
    val defaultNavigationApp: StateFlow<String?> = navigationService.defaultNavigationApp
    val navigationApps: StateFlow<List<NavigationAppInfo>> = navigationService.navigationApps
    val navigationGuidance: StateFlow<NavigationState> = navigationService.navigationState

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

    fun launchNavigation(): Boolean = navigationService.launchNavigation()
    fun searchDestination(query: String): Boolean = navigationService.searchDestination(query)
    fun openNavigationApp(packageName: String): Boolean = navigationService.openNavigationApp(packageName)
    fun setPreferredNavigationApp(packageName: String) = navigationService.setPreferredNavigationApp(packageName)
    fun refreshNavigationApps() = navigationService.refreshNavigationApps()

    fun openBluetoothSettings(): Boolean = bluetoothService.openBluetoothSettings()

    fun launchDialer(number: String? = null): Boolean = phoneService.launchDialer(number)
    fun makeCall(number: String): Boolean = phoneService.makeCall(number)

    fun launchZLink(): Boolean = zlinkService.launchZLink()

    fun launchApp(packageName: String): Boolean = appLauncherService.launchApp(packageName)

    fun refreshApps() = appLauncherService.refreshApps()

    fun launchRadioApp(): Boolean = radioService.launchRadioApp()
    fun tuneRadio(frequency: Double) = radioService.tune(frequency)
    fun seekRadioNext() = radioService.seekNext()
    fun seekRadioPrevious() = radioService.seekPrevious()
    fun refreshRadioState() = radioService.refreshRadioState()

    fun setDriverTemperature(tempCelsius: Float): Boolean = climateService.setDriverTemperature(tempCelsius)
    fun adjustDriverTemperature(deltaCelsius: Float): Boolean = climateService.adjustDriverTemperature(deltaCelsius)
    fun setPassengerTemperature(tempCelsius: Float): Boolean = climateService.setPassengerTemperature(tempCelsius)
    fun adjustPassengerTemperature(deltaCelsius: Float): Boolean = climateService.adjustPassengerTemperature(deltaCelsius)
    fun setFanSpeed(speed: Int): Boolean = climateService.setFanSpeed(speed)
    fun adjustFanSpeed(delta: Int): Boolean = climateService.adjustFanSpeed(delta)
    fun toggleAc(): Boolean = climateService.toggleAc()
    fun toggleAuto(): Boolean = climateService.toggleAuto()
    fun toggleFrontDefrost(): Boolean = climateService.toggleFrontDefrost()
    fun toggleRearDefrost(): Boolean = climateService.toggleRearDefrost()
    fun toggleRecirculation(): Boolean = climateService.toggleRecirculation()
    fun cycleAirflow(): Boolean = climateService.cycleAirflow()
    fun refreshClimateState() = climateService.refreshClimateState()

    fun playPauseMedia() {
        val current = currentMedia.value
        if (current?.isPlaying == true) {
            mediaService.pause()
        } else {
            mediaService.play()
        }
    }

    fun playMedia() = mediaService.play()
    fun pauseMedia() = mediaService.pause()
    fun nextMedia() = mediaService.next()
    fun previousMedia() = mediaService.previous()
    fun seekMediaTo(positionMs: Long) = mediaService.seekTo(positionMs)
    fun refreshMediaSessions() = mediaService.refreshSessions()
}
