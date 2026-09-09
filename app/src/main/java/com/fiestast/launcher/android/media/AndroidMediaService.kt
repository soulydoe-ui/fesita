package com.fiestast.launcher.android.media

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.fiestast.launcher.android.notifications.LauncherNotificationListener
import com.fiestast.launcher.android.notifications.NotificationListenerBridge
import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.MediaService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidMediaService(
    private val context: Context
) : MediaService {

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    private val mediaSessionManager: MediaSessionManager? = try {
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    } catch (_: Throwable) {
        null
    }

    private val notificationListenerComponent = ComponentName(context, LauncherNotificationListener::class.java)

    private val _status = MutableStateFlow<ServiceStatus>(
        ServiceStatus.Available("No active media player")
    )
    override val status: StateFlow<ServiceStatus> = _status.asStateFlow()

    private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
    override val currentMedia: StateFlow<MediaInfo?> = _currentMedia.asStateFlow()

    private var activeController: MediaController? = null
    private var isSessionListenerRegistered = false
    private var progressTickerJob: Job? = null

    // Artwork cache
    private var cachedArtwork: Bitmap? = null
    private var cachedArtworkKey: String? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            Log.d(TAG, "PlaybackState changed: ${state?.state}")
            updateMediaFromController(activeController)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            Log.d(TAG, "MediaMetadata changed: ${metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)}")
            updateMediaFromController(activeController)
        }

        override fun onSessionDestroyed() {
            Log.d(TAG, "MediaSession destroyed.")
            detachActiveController()
            refreshSessions()
        }
    }

    private val activeSessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        Log.d(TAG, "Active sessions changed: count=${controllers?.size ?: 0}")
        serviceScope.launch {
            handleControllersChanged(controllers ?: emptyList())
        }
    }

    private val notificationBridgeCallback = object : NotificationListenerBridge.NotificationListenerCallback {
        override fun onListenerConnected() {
            Log.d(TAG, "Notification listener connected - binding MediaSessionManager")
            registerSessionListenerIfPossible()
            refreshSessions()
        }

        override fun onListenerDisconnected() {
            Log.d(TAG, "Notification listener disconnected")
            detachActiveController()
            _status.value = ServiceStatus.Unavailable("Notification listener disconnected")
            _currentMedia.value = null
        }

        override fun onNotificationChanged() {
            // Check for newly started media playback from player notifications
            refreshSessions()
        }
    }

    init {
        NotificationListenerBridge.register(notificationBridgeCallback)
        registerSessionListenerIfPossible()
        refreshSessions()
    }

    private fun registerSessionListenerIfPossible() {
        if (isSessionListenerRegistered || mediaSessionManager == null) return

        val isGranted = LauncherNotificationListener.isNotificationAccessGranted(context)
        if (!isGranted) {
            _status.value = ServiceStatus.Unavailable("Notification access required for MediaSession")
            return
        }

        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionsChangedListener,
                notificationListenerComponent,
                mainHandler
            )
            isSessionListenerRegistered = true
            Log.d(TAG, "Successfully registered onActiveSessionsChangedListener")
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification access permission required: ${e.message}")
            _status.value = ServiceStatus.Unavailable("Notification access required")
        } catch (t: Throwable) {
            Log.w(TAG, "Could not register active sessions listener: ${t.message}")
        }
    }

    override fun refreshSessions() {
        registerSessionListenerIfPossible()

        val isGranted = LauncherNotificationListener.isNotificationAccessGranted(context)
        if (!isGranted) {
            detachActiveController()
            _status.value = ServiceStatus.Unavailable("Notification access required for MediaSession")
            _currentMedia.value = null
            return
        }

        try {
            val controllers = mediaSessionManager?.getActiveSessions(notificationListenerComponent) ?: emptyList()
            handleControllersChanged(controllers)
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException reading active sessions: ${e.message}")
            detachActiveController()
            _status.value = ServiceStatus.Unavailable("Notification access required")
            _currentMedia.value = null
        } catch (t: Throwable) {
            Log.w(TAG, "Error querying active sessions: ${t.message}")
            detachActiveController()
            _status.value = ServiceStatus.Unavailable("MediaSession error: ${t.message}")
            _currentMedia.value = null
        }
    }

    private fun handleControllersChanged(controllers: List<MediaController>) {
        if (controllers.isEmpty()) {
            detachActiveController()
            _currentMedia.value = null
            _status.value = ServiceStatus.Available("No active media sessions")
            return
        }

        // 1. Prefer existing controller if still present and playing
        val existingPlaying = activeController?.let { curr ->
            controllers.find { it.sessionToken == curr.sessionToken && it.playbackState?.state == PlaybackState.STATE_PLAYING }
        }

        // 2. Any other controller currently playing
        val anyPlaying = controllers.find { it.playbackState?.state == PlaybackState.STATE_PLAYING }

        // 3. Any controller paused/buffering with non-blank title
        val pausedWithMetadata = controllers.find {
            val s = it.playbackState?.state
            (s == PlaybackState.STATE_PAUSED || s == PlaybackState.STATE_BUFFERING) && hasValidMetadata(it.metadata)
        }

        // 4. Any controller paused/buffering
        val anyPaused = controllers.find {
            val s = it.playbackState?.state
            s == PlaybackState.STATE_PAUSED || s == PlaybackState.STATE_BUFFERING
        }

        // 5. Existing controller if still in list
        val existingPresent = activeController?.let { curr ->
            controllers.find { it.sessionToken == curr.sessionToken }
        }

        // 6. First controller with valid metadata
        val firstWithMetadata = controllers.find { hasValidMetadata(it.metadata) }

        val targetController: MediaController = existingPlaying
            ?: anyPlaying
            ?: pausedWithMetadata
            ?: anyPaused
            ?: existingPresent
            ?: firstWithMetadata
            ?: controllers.first()

        attachController(targetController)
    }

    private fun hasValidMetadata(metadata: MediaMetadata?): Boolean {
        if (metadata == null) return false
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: metadata.description?.title?.toString()
        return !title.isNullOrBlank()
    }

    private fun attachController(newController: MediaController) {
        if (activeController?.sessionToken == newController.sessionToken) {
            // Already attached, update state
            updateMediaFromController(newController)
            return
        }

        detachActiveController()
        activeController = newController

        try {
            newController.registerCallback(controllerCallback, mainHandler)
        } catch (t: Throwable) {
            Log.w(TAG, "Error registering MediaController callback: ${t.message}")
        }

        updateMediaFromController(newController)
    }

    private fun detachActiveController() {
        stopProgressTicker()
        try {
            activeController?.unregisterCallback(controllerCallback)
        } catch (_: Throwable) {}
        activeController = null
    }

    private fun updateMediaFromController(controller: MediaController?) {
        if (controller == null) {
            _currentMedia.value = null
            _status.value = ServiceStatus.Available("No active media player")
            stopProgressTicker()
            return
        }

        val metadata = controller.metadata
        val state = controller.playbackState

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: metadata?.description?.title?.toString()

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: metadata?.description?.subtitle?.toString()

        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
            ?: metadata?.description?.description?.toString()

        // When no metadata is present and controller is not actively playing, show honest empty state
        if (title.isNullOrBlank() && artist.isNullOrBlank() && (state == null || state.state != PlaybackState.STATE_PLAYING)) {
            _currentMedia.value = null
            _status.value = ServiceStatus.Available("No media playing")
            stopProgressTicker()
            return
        }

        val isPlaying = state?.state == PlaybackState.STATE_PLAYING
        val playbackStateStr = when (state?.state) {
            PlaybackState.STATE_PLAYING -> "Playing"
            PlaybackState.STATE_PAUSED -> "Paused"
            PlaybackState.STATE_STOPPED -> "Stopped"
            PlaybackState.STATE_BUFFERING -> "Buffering"
            PlaybackState.STATE_CONNECTING -> "Connecting"
            PlaybackState.STATE_FAST_FORWARDING -> "Fast Forward"
            PlaybackState.STATE_REWINDING -> "Rewind"
            PlaybackState.STATE_SKIPPING_TO_NEXT -> "Next"
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS -> "Previous"
            else -> if (isPlaying) "Playing" else "Stopped"
        }

        val rawPosition = state?.position ?: 0L
        val durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val currentPositionMs = if (isPlaying && state != null && state.lastPositionUpdateTime > 0) {
            val delta = SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
            val estimated = rawPosition + (delta * state.playbackSpeed).toLong()
            if (durationMs > 0) estimated.coerceIn(0L, durationMs) else maxOf(0L, estimated)
        } else {
            maxOf(0L, rawPosition)
        }

        val actions = state?.actions ?: 0L
        val canPlay = (actions and (PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)) != 0L || actions == 0L
        val canPause = (actions and (PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE)) != 0L || actions == 0L
        val canSkipNext = (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L || actions == 0L
        val canSkipPrevious = (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L || actions == 0L
        val canSeek = (actions and PlaybackState.ACTION_SEEK_TO) != 0L

        val artwork = extractArtworkBitmap(metadata, controller.packageName)

        _currentMedia.value = MediaInfo(
            title = if (!title.isNullOrBlank()) title else "Audio Track",
            artist = if (!artist.isNullOrBlank()) artist else (controller.packageName ?: "Media Player"),
            album = album,
            isPlaying = isPlaying,
            playbackState = playbackStateStr,
            positionMs = currentPositionMs,
            durationMs = if (durationMs > 0) durationMs else 0L,
            artworkBitmap = artwork,
            canPlay = canPlay,
            canPause = canPause,
            canSkipNext = canSkipNext,
            canSkipPrevious = canSkipPrevious,
            canSeek = canSeek,
            packageName = controller.packageName
        )

        _status.value = ServiceStatus.Connected

        if (isPlaying) {
            startProgressTicker()
        } else {
            stopProgressTicker()
        }
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _currentMedia.value
                val controller = activeController
                val state = controller?.playbackState
                if (current != null && current.isPlaying && state != null && state.state == PlaybackState.STATE_PLAYING) {
                    val delta = SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
                    val estimated = state.position + (delta * state.playbackSpeed).toLong()
                    val updated = if (current.durationMs > 0) {
                        estimated.coerceIn(0L, current.durationMs)
                    } else {
                        maxOf(0L, estimated)
                    }
                    _currentMedia.value = current.copy(positionMs = updated)
                } else {
                    break
                }
            }
        }
    }

    private fun stopProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = null
    }

    private fun extractArtworkBitmap(metadata: MediaMetadata?, packageName: String?): Bitmap? {
        if (metadata == null) return null
        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val key = "$packageName:$title:$artist"

        if (key == cachedArtworkKey && cachedArtwork != null) {
            return cachedArtwork
        }

        var bitmap: Bitmap? = null
        try {
            bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata.description?.iconBitmap
        } catch (e: Throwable) {
            Log.w(TAG, "Failed reading bitmap from metadata: ${e.message}")
        }

        if (bitmap != null) {
            val scaled = scaleDownBitmap(bitmap, 512)
            cachedArtwork = scaled
            cachedArtworkKey = key
            return scaled
        }

        // Try artwork URI
        val uriStr = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)
            ?: metadata.description?.iconUri?.toString()

        if (!uriStr.isNullOrBlank()) {
            try {
                val uri = Uri.parse(uriStr)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    val decoded = BitmapFactory.decodeStream(stream, null, options)
                    val scaled = scaleDownBitmap(decoded, 512)
                    cachedArtwork = scaled
                    cachedArtworkKey = key
                    return scaled
                }
            } catch (_: Throwable) {
                // Ignore decoding failures cleanly
            }
        }

        cachedArtwork = null
        cachedArtworkKey = key
        return null
    }

    private fun scaleDownBitmap(bitmap: Bitmap?, maxDim: Int): Bitmap? {
        if (bitmap == null) return null
        if (bitmap.width <= maxDim && bitmap.height <= maxDim) return bitmap

        val ratio = minOf(maxDim.toFloat() / bitmap.width, maxDim.toFloat() / bitmap.height)
        val w = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).toInt().coerceAtLeast(1)

        return try {
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } catch (_: Throwable) {
            bitmap
        }
    }

    override fun play() {
        val controller = activeController
        val canPlay = _currentMedia.value?.canPlay ?: true
        if (controller != null && canPlay) {
            try {
                controller.transportControls.play()
            } catch (t: Throwable) {
                Log.w(TAG, "play() failed: ${t.message}")
            }
        }
    }

    override fun pause() {
        val controller = activeController
        val canPause = _currentMedia.value?.canPause ?: true
        if (controller != null && canPause) {
            try {
                controller.transportControls.pause()
            } catch (t: Throwable) {
                Log.w(TAG, "pause() failed: ${t.message}")
            }
        }
    }

    override fun next() {
        val controller = activeController
        val canNext = _currentMedia.value?.canSkipNext ?: true
        if (controller != null && canNext) {
            try {
                controller.transportControls.skipToNext()
            } catch (t: Throwable) {
                Log.w(TAG, "next() failed: ${t.message}")
            }
        }
    }

    override fun previous() {
        val controller = activeController
        val canPrev = _currentMedia.value?.canSkipPrevious ?: true
        if (controller != null && canPrev) {
            try {
                controller.transportControls.skipToPrevious()
            } catch (t: Throwable) {
                Log.w(TAG, "previous() failed: ${t.message}")
            }
        }
    }

    override fun seekTo(positionMs: Long) {
        val controller = activeController
        val canSeek = _currentMedia.value?.canSeek ?: true
        if (controller != null && canSeek) {
            try {
                controller.transportControls.seekTo(positionMs)
            } catch (t: Throwable) {
                Log.w(TAG, "seekTo() failed: ${t.message}")
            }
        }
    }

    fun cleanup() {
        NotificationListenerBridge.unregister(notificationBridgeCallback)
        detachActiveController()
        if (isSessionListenerRegistered && mediaSessionManager != null) {
            try {
                mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionsChangedListener)
            } catch (_: Throwable) {}
            isSessionListenerRegistered = false
        }
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "FiestaSTMediaService"
    }
}
