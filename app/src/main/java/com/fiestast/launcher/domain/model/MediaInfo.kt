package com.fiestast.launcher.domain.model

data class MediaInfo(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val isPlaying: Boolean = false,
    val playbackState: String = "Stopped"
)
