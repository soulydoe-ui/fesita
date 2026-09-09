package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.BluetoothDeviceInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface BluetoothService {
    val status: StateFlow<ServiceStatus>
    val connectedDeviceName: StateFlow<String?>
    val isBluetoothEnabled: StateFlow<Boolean>
    val isA2dpConnected: StateFlow<Boolean>
    val isHeadsetConnected: StateFlow<Boolean>
    val connectedDevices: StateFlow<List<BluetoothDeviceInfo>>

    fun refreshState()
    fun openBluetoothSettings(): Boolean
    fun cleanup()
}

