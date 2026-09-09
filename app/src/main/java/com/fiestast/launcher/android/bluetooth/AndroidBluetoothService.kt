package com.fiestast.launcher.android.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
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

    private val _isBluetoothEnabled = MutableStateFlow(checkAdapterEnabled())
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkAdapterEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    override fun refreshState() {
        _isBluetoothEnabled.value = checkAdapterEnabled()
        _status.value = determineStatus()
    }

    private fun determineStatus(): ServiceStatus {
        if (bluetoothAdapter == null) {
            return ServiceStatus.Unavailable("Bluetooth hardware not available")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasConnectPermission()) {
            return ServiceStatus.Unavailable("Bluetooth permission required")
        }

        return try {
            if (!bluetoothAdapter.isEnabled) {
                ServiceStatus.Available("Bluetooth is turned off")
            } else {
                ServiceStatus.Available("Ready to pair or connect")
            }
        } catch (e: SecurityException) {
            ServiceStatus.Unavailable("Bluetooth permission missing: ${e.message}")
        } catch (e: Exception) {
            ServiceStatus.Unavailable("Bluetooth error: ${e.message}")
        }
    }
}
