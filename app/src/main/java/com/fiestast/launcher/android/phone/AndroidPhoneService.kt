package com.fiestast.launcher.android.phone

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.fiestast.launcher.domain.model.CallState
import com.fiestast.launcher.domain.model.PhoneCallInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.PhoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidPhoneService(
    private val context: Context
) : PhoneService {

    private val telephonyManager: TelephonyManager? = try {
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    } catch (_: Exception) {
        null
    }

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Available("Phone service ready")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _callState = MutableStateFlow(CallState.IDLE)
    override val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _currentCallInfo = MutableStateFlow(PhoneCallInfo(CallState.IDLE))
    override val currentCallInfo: StateFlow<PhoneCallInfo> = _currentCallInfo.asStateFlow()

    private val _activeCallContact = MutableStateFlow<String?>(null)
    override val activeCallContact: StateFlow<String?> = _activeCallContact.asStateFlow()

    private val _isInCall = MutableStateFlow(false)
    override val isInCall: StateFlow<Boolean> = _isInCall.asStateFlow()

    override val isCallPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

    private var isReceiverRegistered = false
    private var modernTelephonyCallback: Any? = null

    private val phoneBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            if (action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                when (stateStr) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        updateCallState(CallState.RINGING, incomingNumber)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        updateCallState(CallState.ACTIVE, incomingNumber)
                    }
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        updateCallState(CallState.IDLE, null)
                    }
                }
            } else if (action == "android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED") {
                val audioState = intent.getIntExtra("android.bluetooth.profile.extra.STATE", -1)
                // 12 = STATE_AUDIO_CONNECTED
                if (audioState == 12 && _callState.value == CallState.IDLE) {
                    updateCallState(CallState.ACTIVE, null)
                } else if (audioState == 10 && _callState.value == CallState.ACTIVE) {
                    // 10 = STATE_AUDIO_DISCONNECTED
                    updateCallState(CallState.IDLE, null)
                }
            }
        }
    }

    val broadcastReceiver: BroadcastReceiver get() = phoneBroadcastReceiver

    init {
        registerListeners()
    }

    private fun registerListeners() {
        // Register BroadcastReceiver for broadcast state events
        try {
            val filter = IntentFilter().apply {
                addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                addAction("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(phoneBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(phoneBroadcastReceiver, filter)
            }
            isReceiverRegistered = true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register Phone broadcast receiver: ${e.message}")
        }

        // Register modern TelephonyCallback on Android 12+ (API 31+) if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyManager != null) {
            try {
                val callback = ModernCallStateCallback()
                telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
                modernTelephonyCallback = callback
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register modern telephony callback: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private inner class ModernCallStateCallback : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            handleTelephonyCallState(state, null)
        }
    }

    fun handleTelephonyCallState(state: Int, incomingNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                updateCallState(CallState.RINGING, incomingNumber)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                updateCallState(CallState.ACTIVE, incomingNumber)
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                updateCallState(CallState.IDLE, null)
            }
        }
    }

    fun updateCallState(state: CallState, number: String?) {
        _callState.value = state
        when (state) {
            CallState.RINGING -> {
                _isInCall.value = false
                _activeCallContact.value = number
                _currentCallInfo.value = PhoneCallInfo(
                    state = CallState.RINGING,
                    callerNumber = number,
                    callerName = null
                )
                _status.value = ServiceStatus.Connected
            }
            CallState.ACTIVE -> {
                _isInCall.value = true
                val existingNum = _currentCallInfo.value.callerNumber
                val activeNum = number ?: existingNum
                _activeCallContact.value = activeNum
                _currentCallInfo.value = PhoneCallInfo(
                    state = CallState.ACTIVE,
                    callerNumber = activeNum,
                    callerName = null
                )
                _status.value = ServiceStatus.Connected
            }
            CallState.IDLE -> {
                _isInCall.value = false
                _activeCallContact.value = null
                _currentCallInfo.value = PhoneCallInfo(state = CallState.IDLE)
                _status.value = ServiceStatus.Available("Phone service ready")
            }
        }
    }

    override fun makeCall(number: String): Boolean {
        if (number.isBlank()) {
            return launchDialer()
        }

        val sanitizedNumber = number.trim()

        if (isCallPermissionGranted) {
            try {
                val uri = Uri.parse("tel:${Uri.encode(sanitizedNumber)}")
                val intent = Intent(Intent.ACTION_CALL, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: SecurityException) {
                // Fall back safely to ACTION_DIAL
                return launchDialer(sanitizedNumber)
            } catch (_: Exception) {
                return launchDialer(sanitizedNumber)
            }
        }

        // When CALL_PHONE is not granted or call activity is not resolved, fall back safely to ACTION_DIAL
        return launchDialer(sanitizedNumber)
    }

    override fun launchDialer(number: String?): Boolean {
        return try {
            val intent = if (!number.isNullOrBlank()) {
                val uri = Uri.parse("tel:${Uri.encode(number.trim())}")
                Intent(Intent.ACTION_DIAL, uri)
            } else {
                Intent(Intent.ACTION_DIAL)
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                try {
                    context.startActivity(intent)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    override fun cleanup() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(phoneBroadcastReceiver)
                isReceiverRegistered = false
            } catch (_: Exception) {
                isReceiverRegistered = false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && modernTelephonyCallback != null) {
            try {
                (modernTelephonyCallback as? TelephonyCallback)?.let {
                    telephonyManager?.unregisterTelephonyCallback(it)
                }
                modernTelephonyCallback = null
            } catch (_: Exception) {}
        }
    }

    companion object {
        private const val TAG = "FiestaPhoneService"
    }
}
