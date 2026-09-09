package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.RadioBackendInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface RadioService {
    val status: StateFlow<ServiceStatus>
    val currentFrequency: StateFlow<Double?>
    val detectedRadioPackage: StateFlow<String?> get() = MutableStateFlow(null)
    val detectedBackendInfo: StateFlow<RadioBackendInfo?> get() = MutableStateFlow(null)
    val canControlHardware: StateFlow<Boolean> get() = MutableStateFlow(false)

    fun tune(frequency: Double)
    fun seekNext()
    fun seekPrevious()
    fun launchRadioApp(): Boolean = false
    fun refreshRadioState() {}
}

