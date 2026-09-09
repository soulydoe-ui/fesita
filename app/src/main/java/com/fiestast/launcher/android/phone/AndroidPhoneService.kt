package com.fiestast.launcher.android.phone

import android.content.Context
import android.content.Intent
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.PhoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidPhoneService(
    private val context: Context
) : PhoneService {

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Available("Phone service ready")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    // Real phone state: null and false when idle. Never fake phone numbers or contacts.
    override val activeCallContact: StateFlow<String?> = MutableStateFlow(null)
    override val isInCall: StateFlow<Boolean> = MutableStateFlow(false)

    override fun launchDialer(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
