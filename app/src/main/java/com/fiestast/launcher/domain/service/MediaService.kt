package com.fiestast.launcher.domain.service

import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import kotlinx.coroutines.flow.StateFlow

interface MediaService {
    val status: StateFlow<ServiceStatus>
    val currentMedia: StateFlow<MediaInfo?>
    fun play()
    fun pause()
    fun next()
    fun previous()
}
