package com.fiestast.launcher.android.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.fiestast.launcher.domain.model.BluetoothDeviceInfo
import com.fiestast.launcher.domain.model.BluetoothProfileType
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

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(checkAdapterEnabled())
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _isA2dpConnected = MutableStateFlow(false)
    override val isA2dpConnected: StateFlow<Boolean> = _isA2dpConnected.asStateFlow()

    private val _isHeadsetConnected = MutableStateFlow(false)
    override val isHeadsetConnected: StateFlow<Boolean> = _isHeadsetConnected.asStateFlow()

    private val _connectedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    override val connectedDevices: StateFlow<List<BluetoothDeviceInfo>> = _connectedDevices.asStateFlow()

    @Volatile
    var a2dpProfile: BluetoothA2dp? = null
        private set

    @Volatile
    var headsetProfile: BluetoothHeadset? = null
        private set

    private var isReceiverRegistered = false

    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            when (profile) {
                BluetoothProfile.A2DP -> {
                    a2dpProfile = proxy as? BluetoothA2dp
                    queryConnectedDevices()
                }
                BluetoothProfile.HEADSET -> {
                    headsetProfile = proxy as? BluetoothHeadset
                    queryConnectedDevices()
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            when (profile) {
                BluetoothProfile.A2DP -> {
                    a2dpProfile = null
                    queryConnectedDevices()
                }
                BluetoothProfile.HEADSET -> {
                    headsetProfile = null
                    queryConnectedDevices()
                }
            }
        }
    }

    private val bluetoothBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val isEnabled = (state == BluetoothAdapter.STATE_ON)
                    _isBluetoothEnabled.value = isEnabled
                    if (isEnabled) {
                        bindProfileProxies()
                    }
                    queryConnectedDevices(overrideEnabled = isEnabled)
                }
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                    queryConnectedDevices()
                }
            }
        }
    }

    val broadcastReceiver: BroadcastReceiver get() = bluetoothBroadcastReceiver

    init {
        registerReceiverSafely()
        bindProfileProxies()
        queryConnectedDevices()
    }

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

    private fun bindProfileProxies() {
        if (bluetoothAdapter == null) return
        try {
            bluetoothAdapter.getProfileProxy(context, profileServiceListener, BluetoothProfile.A2DP)
            bluetoothAdapter.getProfileProxy(context, profileServiceListener, BluetoothProfile.HEADSET)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to bind Bluetooth profile proxies: ${e.message}")
        }
    }

    private fun registerReceiverSafely() {
        if (isReceiverRegistered) return
        try {
            val filter = IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(bluetoothBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(bluetoothBroadcastReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register Bluetooth broadcast receiver: ${e.message}")
        }
    }

    private fun unregisterReceiverSafely() {
        if (!isReceiverRegistered) return
        try {
            context.unregisterReceiver(bluetoothBroadcastReceiver)
            isReceiverRegistered = false
        } catch (_: Exception) {
            isReceiverRegistered = false
        }
    }

    @SuppressLint("MissingPermission")
    fun queryConnectedDevices(overrideEnabled: Boolean? = null) {
        val adapter = bluetoothAdapter
        val isEnabled = overrideEnabled ?: checkAdapterEnabled()
        _isBluetoothEnabled.value = isEnabled

        if (adapter == null) {
            _connectedDevices.value = emptyList()
            _connectedDeviceName.value = null
            _isA2dpConnected.value = false
            _isHeadsetConnected.value = false
            _status.value = determineStatus()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasConnectPermission()) {
            _connectedDevices.value = emptyList()
            _connectedDeviceName.value = null
            _isA2dpConnected.value = false
            _isHeadsetConnected.value = false
            _status.value = ServiceStatus.Unavailable("Bluetooth permission required")
            return
        }

        if (!isEnabled) {
            _connectedDevices.value = emptyList()
            _connectedDeviceName.value = null
            _isA2dpConnected.value = false
            _isHeadsetConnected.value = false
            _status.value = ServiceStatus.Available("Bluetooth is turned off")
            return
        }

        try {
            // Group by physical device identifier in memory, exposing only user-facing device names to domain model
            val deviceMap = mutableMapOf<String, BluetoothDeviceInfo>()
            var a2dpActive = false
            var headsetActive = false

            // Query real A2DP connected devices
            a2dpProfile?.let { a2dp ->
                val list = a2dp.connectedDevices
                if (list.isNotEmpty()) {
                    a2dpActive = true
                }
                for (dev in list) {
                    val name = dev.name?.takeIf { it.isNotBlank() } ?: "Bluetooth Audio Device"
                    val key = dev.address ?: name
                    val existing = deviceMap[key]
                    val profiles = (existing?.supportedProfiles ?: emptySet()) + BluetoothProfileType.A2DP
                    deviceMap[key] = BluetoothDeviceInfo(
                        name = name,
                        isConnected = true,
                        supportedProfiles = profiles,
                        isA2dpConnected = true,
                        isHeadsetConnected = existing?.isHeadsetConnected ?: false
                    )
                }
            }

            // Query real HEADSET connected devices
            headsetProfile?.let { headset ->
                val list = headset.connectedDevices
                if (list.isNotEmpty()) {
                    headsetActive = true
                }
                for (dev in list) {
                    val name = dev.name?.takeIf { it.isNotBlank() } ?: "Bluetooth Phone Device"
                    val key = dev.address ?: name
                    val existing = deviceMap[key]
                    val profiles = (existing?.supportedProfiles ?: emptySet()) + BluetoothProfileType.HEADSET
                    deviceMap[key] = BluetoothDeviceInfo(
                        name = name,
                        isConnected = true,
                        supportedProfiles = profiles,
                        isA2dpConnected = existing?.isA2dpConnected ?: false,
                        isHeadsetConnected = true
                    )
                }
            }

            val deviceList = deviceMap.values.toList()
            _connectedDevices.value = deviceList
            _connectedDeviceName.value = deviceList.firstOrNull()?.name
            _isA2dpConnected.value = a2dpActive
            _isHeadsetConnected.value = headsetActive
            _status.value = determineStatus()
        } catch (e: SecurityException) {
            _connectedDevices.value = emptyList()
            _connectedDeviceName.value = null
            _status.value = ServiceStatus.Unavailable("Bluetooth permission missing: ${e.message}")
        } catch (e: Exception) {
            _connectedDevices.value = emptyList()
            _connectedDeviceName.value = null
            _status.value = ServiceStatus.Unavailable("Bluetooth query error: ${e.message}")
        }
    }

    override fun refreshState() {
        _isBluetoothEnabled.value = checkAdapterEnabled()
        queryConnectedDevices()
    }

    override fun openBluetoothSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun cleanup() {
        unregisterReceiverSafely()
        try {
            a2dpProfile?.let { proxy ->
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, proxy)
            }
            a2dpProfile = null
            headsetProfile?.let { proxy ->
                bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
            }
            headsetProfile = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing Bluetooth profile proxies: ${e.message}")
        }
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
                val count = _connectedDevices.value.size
                if (count > 0) {
                    ServiceStatus.Connected
                } else {
                    ServiceStatus.Available("Ready to pair or connect")
                }
            }
        } catch (e: SecurityException) {
            ServiceStatus.Unavailable("Bluetooth permission missing: ${e.message}")
        } catch (e: Exception) {
            ServiceStatus.Unavailable("Bluetooth error: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "FiestaBluetoothService"
    }
}
