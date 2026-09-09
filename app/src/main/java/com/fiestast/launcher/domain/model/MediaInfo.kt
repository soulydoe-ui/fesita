package com.fiestast.launcher.domain.model

import android.graphics.Bitmap

data class MediaInfo(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val isPlaying: Boolean = false,
    val playbackState: String = "Stopped",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val artworkBitmap: Bitmap? = null,
    val canPlay: Boolean = true,
    val canPause: Boolean = true,
    val canSkipNext: Boolean = true,
    val canSkipPrevious: Boolean = true,
    val canSeek: Boolean = false,
    val packageName: String? = null
)
