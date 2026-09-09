package com.fiestast.launcher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.ui.theme.BrightRed
import com.fiestast.launcher.ui.theme.CardBorder
import com.fiestast.launcher.ui.theme.LightGray
import com.fiestast.launcher.ui.theme.PureWhite
import java.util.Locale

/**
 * Music Player Widget for the Home screen:
 * - Shows authentic MediaSession track details when an active media session exists
 * - When no media is playing, displays honest "NO ACTIVE MEDIA" empty state
 *   rather than faking "The Weeknd / Blinding Lights"
 * - Connects to real Android MediaSession playback controls
 */
@Composable
fun MockupMusicWidget(
    status: ServiceStatus,
    mediaInfo: MediaInfo?,
    onNavigateSection: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val hasActiveMedia = status is ServiceStatus.Connected && mediaInfo != null && !mediaInfo.title.isNullOrBlank()
    val isPlaying = mediaInfo?.isPlaying == true

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1116))
            .border(1.dp, CardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onNavigateSection() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("mockup_music_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Row: Album Art + Track Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art / Vinyl Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161922))
                        .border(1.dp, if (hasActiveMedia) BrightRed.copy(alpha = 0.6f) else CardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasActiveMedia && mediaInfo?.artworkBitmap != null) {
                        Image(
                            bitmap = mediaInfo.artworkBitmap.asImageBitmap(),
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        if (hasActiveMedia) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (size.width < 5f || size.height < 5f) return@Canvas
                                try {
                                    val w = size.width
                                    val h = size.height
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(BrightRed.copy(alpha = 0.7f), Color(0xFF161922)),
                                            center = Offset(w * 0.5f, h * 0.5f),
                                            radius = maxOf(1f, w * 0.5f)
                                        )
                                    )
                                } catch (_: Throwable) {
                                    // Fail safely
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music Art",
                            tint = if (hasActiveMedia) PureWhite else LightGray.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title, Artist, Album or "NO ACTIVE MEDIA"
                Column(modifier = Modifier.weight(1f)) {
                    if (hasActiveMedia) {
                        Text(
                            text = mediaInfo?.artist ?: "Unknown Artist",
                            color = LightGray.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = mediaInfo?.title ?: "Track",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = mediaInfo?.album ?: "",
                            color = LightGray.copy(alpha = 0.7f),
                            fontSize = 9.5.sp,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "NO ACTIVE MEDIA",
                            color = PureWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to launch media player",
                            color = LightGray.copy(alpha = 0.75f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 2. Playback Scrubber Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                val progressFraction = if (hasActiveMedia && mediaInfo != null && mediaInfo.durationMs > 0) {
                    (mediaInfo.positionMs.toFloat() / mediaInfo.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color(0xFF232834))
                ) {
                    if (hasActiveMedia && progressFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(3.dp)
                                .background(BrightRed)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val posText = if (hasActiveMedia && mediaInfo != null) formatTimeMs(mediaInfo.positionMs) else "--:--"
                    val durText = if (hasActiveMedia && mediaInfo != null && mediaInfo.durationMs > 0) formatTimeMs(mediaInfo.durationMs) else "--:--"
                    Text(
                        text = posText,
                        color = LightGray.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                    Text(
                        text = durText,
                        color = LightGray.copy(alpha = 0.6f),
                        fontSize = 9.sp
                    )
                }
            }

            // 3. Playback Controls Row (Previous, Play/Pause, Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Track
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161922))
                        .clickable(enabled = mediaInfo?.canSkipPrevious ?: true) { onPrevious() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play / Pause Button (Wide glowing red pill matching master mockup)
                Box(
                    modifier = Modifier
                        .width(58.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                            )
                        )
                        .border(1.dp, BrightRed, RoundedCornerShape(17.dp))
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Next Track
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161922))
                        .clickable(enabled = mediaInfo?.canSkipNext ?: true) { onNext() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimeMs(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSec = millis / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}
