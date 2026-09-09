package com.fiestast.launcher.android.radio

import android.content.Context
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.RadioService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidRadioService(
    private val context: Context
) : RadioService {

    // Radio hardware is head-unit specific. Without proprietary OEM FM radio service,
    // report Unavailable rather than displaying a fake FM frequency.
    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Unavailable("Proprietary head unit FM radio API not present")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _currentFrequency = MutableStateFlow<Double?>(null)
    override val currentFrequency: StateFlow<Double?> = _currentFrequency.asStateFlow()

    override fun tune(frequency: Double) {
        // Integration point for head unit FM broadcast intent
    }

    override fun seekNext() {
        // Integration point for head unit FM broadcast intent
    }

    override fun seekPrevious() {
        // Integration point for head unit FM broadcast intent
    }
}
