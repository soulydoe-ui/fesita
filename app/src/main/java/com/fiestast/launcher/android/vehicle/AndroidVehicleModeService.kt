package com.fiestast.launcher.android.vehicle

import com.fiestast.launcher.data.repository.LauncherPreferencesRepository
import com.fiestast.launcher.domain.model.DriverMode
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.VehicleModeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidVehicleModeService(
    private val repository: LauncherPreferencesRepository
) : VehicleModeService {

    // Note: Driver mode in this launcher is currently a UI profile.
    // Real ECU/CAN bus engine calibration requires verified head-unit vehicle API.
    override val status: StateFlow<ServiceStatus> = MutableStateFlow(
        ServiceStatus.Available("Launcher UI Profile Mode (No ECU/CAN bus connection)")
    )

    override val currentMode: StateFlow<DriverMode> = MutableStateFlow(
        repository.preferences.value.selectedDriverMode
    )

    override fun setDriverMode(mode: DriverMode) {
        repository.setDriverMode(mode)
        (currentMode as MutableStateFlow).value = mode
    }
}
