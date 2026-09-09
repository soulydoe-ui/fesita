package com.fiestast.launcher.android.media

import android.content.Context
import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.MediaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidMediaService(
    private val context: Context
) : MediaService {

    // Real state: initial state indicates no active Android MediaSession detected.
    // Never display fake track names or fake artists.
    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Unavailable("No active Android MediaSession detected")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
    override val currentMedia: StateFlow<MediaInfo?> = _currentMedia.asStateFlow()

    override fun play() {
        // Real implementation point for MediaController.transportControls
    }

    override fun pause() {
        // Real implementation point for MediaController.transportControls
    }

    override fun next() {
        // Real implementation point for MediaController.transportControls
    }

    override fun previous() {
        // Real implementation point for MediaController.transportControls
    }
}
