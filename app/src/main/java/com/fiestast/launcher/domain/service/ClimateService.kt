package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface ClimateService {
    val status: StateFlow<ServiceStatus>
    val driverTempCelsius: StateFlow<Float?>
    val passengerTempCelsius: StateFlow<Float?>
    val fanSpeed: StateFlow<Int?>
    val isAcOn: StateFlow<Boolean?>
}
