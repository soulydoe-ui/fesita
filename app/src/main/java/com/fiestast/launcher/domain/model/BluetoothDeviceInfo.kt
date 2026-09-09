package com.fiestast.launcher.domain.model

enum class BluetoothProfileType {
    A2DP,
    HEADSET
}

data class BluetoothDeviceInfo(
    val name: String,
    val isConnected: Boolean = true,
    val supportedProfiles: Set<BluetoothProfileType> = emptySet(),
    val isA2dpConnected: Boolean = false,
    val isHeadsetConnected: Boolean = false
)
