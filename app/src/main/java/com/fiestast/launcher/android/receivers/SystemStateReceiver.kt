package com.fiestast.launcher.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SystemPowerState(
    val isPowerConnected: Boolean = false,
    val isBatteryLow: Boolean = false
)

/**
 * Defensive receiver for system power, battery, and time/timezone events.
 */
class SystemStateReceiver(
    private val onTimeOrTimezoneChanged: () -> Unit
) : BroadcastReceiver() {

    private val _powerState = MutableStateFlow(SystemPowerState())
    val powerState: StateFlow<SystemPowerState> = _powerState.asStateFlow()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        when (intent.action) {
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.d(TAG, "Time or timezone change detected: ${intent.action}")
                onTimeOrTimezoneChanged()
            }
            Intent.ACTION_POWER_CONNECTED -> {
                _powerState.value = _powerState.value.copy(isPowerConnected = true)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                _powerState.value = _powerState.value.copy(isPowerConnected = false)
            }
            Intent.ACTION_BATTERY_LOW -> {
                _powerState.value = _powerState.value.copy(isBatteryLow = true)
            }
            Intent.ACTION_BATTERY_OKAY -> {
                _powerState.value = _powerState.value.copy(isBatteryLow = false)
            }
        }
    }

    fun register(context: Context) {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_BATTERY_OKAY)
            }
            context.registerReceiver(this, filter)
        } catch (t: Throwable) {
            Log.w(TAG, "Could not register SystemStateReceiver: ${t.message}")
        }
    }

    fun unregister(context: Context) {
        try {
            context.unregisterReceiver(this)
        } catch (_: Throwable) {
            // Already unregistered or context destroyed
        }
    }

    companion object {
        private const val TAG = "FiestaSTSystemState"
    }
}
