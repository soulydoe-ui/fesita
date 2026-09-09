package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface RadioService {
    val status: StateFlow<ServiceStatus>
    val currentFrequency: StateFlow<Double?>
    fun tune(frequency: Double)
    fun seekNext()
    fun seekPrevious()
}
