package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface VehicleModeService {
    val status: StateFlow<ServiceStatus>
    val currentMode: StateFlow<DriverMode>
    fun setDriverMode(mode: DriverMode)
}
