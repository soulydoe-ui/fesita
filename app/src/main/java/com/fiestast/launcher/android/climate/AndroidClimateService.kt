package com.fiestast.launcher.android.climate

import android.content.Context
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.ClimateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidClimateService(
    private val context: Context
) : ClimateService {

    // Head-unit climate data requires vehicle CAN bus decoder or Android Car API.
    // Explicitly report Unavailable with reason rather than faking temperature.
    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Unavailable("Vehicle CAN bus climate interface not connected")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    override val driverTempCelsius: StateFlow<Float?> = MutableStateFlow(null)
    override val passengerTempCelsius: StateFlow<Float?> = MutableStateFlow(null)
    override val fanSpeed: StateFlow<Int?> = MutableStateFlow(null)
    override val isAcOn: StateFlow<Boolean?> = MutableStateFlow(null)
}
