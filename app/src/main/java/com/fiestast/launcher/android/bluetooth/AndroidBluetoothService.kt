package com.fiestast.launcher.android.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.BluetoothService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidBluetoothService(
    private val context: Context
) : BluetoothService {

    private val bluetoothAdapter: BluetoothAdapter? = try {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        manager?.adapter
    } catch (_: Exception) {
        null
    }

    private val _status = MutableStateFlow<ServiceStatus>(determineStatus())
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    // Real device name: null when no active device connected (never fake a phone connection)
    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private fun determineStatus(): ServiceStatus {
        return when {
            bluetoothAdapter == null -> ServiceStatus.Unavailable("Bluetooth hardware not available")
            !bluetoothAdapter.isEnabled -> ServiceStatus.Available("Bluetooth is turned off")
            else -> ServiceStatus.Available("Ready to pair or connect")
        }
    }
}
