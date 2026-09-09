package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.AirflowDirection
import com.fiestast.launcher.domain.model.ClimateBackendInfo
import com.fiestast.launcher.domain.model.ClimateState
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ClimateService {
    val status: StateFlow<ServiceStatus>
    val driverTempCelsius: StateFlow<Float?>
    val passengerTempCelsius: StateFlow<Float?>
    val fanSpeed: StateFlow<Int?>
    val isAcOn: StateFlow<Boolean?>

    val climateState: StateFlow<ClimateState> get() = MutableStateFlow(ClimateState())
    val isAutoOn: StateFlow<Boolean?> get() = MutableStateFlow(null)
    val isFrontDefrostOn: StateFlow<Boolean?> get() = MutableStateFlow(null)
    val isRearDefrostOn: StateFlow<Boolean?> get() = MutableStateFlow(null)
    val isRecirculationOn: StateFlow<Boolean?> get() = MutableStateFlow(null)
    val airflowDirection: StateFlow<AirflowDirection> get() = MutableStateFlow(AirflowDirection.UNKNOWN)
    val detectedBackend: StateFlow<ClimateBackendInfo?> get() = MutableStateFlow(null)
    val canControlHardware: StateFlow<Boolean> get() = MutableStateFlow(false)

    fun setDriverTemperature(tempCelsius: Float): Boolean = false
    fun adjustDriverTemperature(deltaCelsius: Float): Boolean = false
    fun setPassengerTemperature(tempCelsius: Float): Boolean = false
    fun adjustPassengerTemperature(deltaCelsius: Float): Boolean = false
    fun setFanSpeed(speed: Int): Boolean = false
    fun adjustFanSpeed(delta: Int): Boolean = false
    fun toggleAc(): Boolean = false
    fun toggleAuto(): Boolean = false
    fun toggleFrontDefrost(): Boolean = false
    fun toggleRearDefrost(): Boolean = false
    fun toggleRecirculation(): Boolean = false
    fun cycleAirflow(): Boolean = false
    fun refreshClimateState() {}
}
