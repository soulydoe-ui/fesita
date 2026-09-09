package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface BluetoothService {
    val status: StateFlow<ServiceStatus>
    val connectedDeviceName: StateFlow<String?>
    val isBluetoothEnabled: StateFlow<Boolean>
    fun refreshState()
}
