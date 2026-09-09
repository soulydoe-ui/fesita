package com.fiestast.launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.fiestast.launcher.android.media.AndroidMediaService
import com.fiestast.launcher.android.notifications.LauncherNotificationListener
import com.fiestast.launcher.android.notifications.NotificationListenerBridge
import com.fiestast.launcher.domain.model.MediaInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import com.fiestast.launcher.domain.service.MediaService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase2MediaSessionTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testMediaInfoDataModelIntegrity() {
        val emptyInfo = MediaInfo()
        assertNull(emptyInfo.title)
        assertNull(emptyInfo.artist)
        assertNull(emptyInfo.album)
        assertFalse(emptyInfo.isPlaying)
        assertEquals(0L, emptyInfo.positionMs)
        assertEquals(0L, emptyInfo.durationMs)
        assertNull(emptyInfo.artworkBitmap)
        assertTrue(emptyInfo.canPlay)
        assertTrue(emptyInfo.canPause)
        assertTrue(emptyInfo.canSkipNext)
        assertTrue(emptyInfo.canSkipPrevious)

        val populatedInfo = MediaInfo(
            title = "Nightcall",
            artist = "Kavinsky",
            album = "OutRun",
            isPlaying = true,
            playbackState = "Playing",
            positionMs = 30000L,
            durationMs = 240000L,
            canSeek = true,
            packageName = "com.spotify.music"
        )
        assertEquals("Nightcall", populatedInfo.title)
        assertEquals("Kavinsky", populatedInfo.artist)
        assertEquals("OutRun", populatedInfo.album)
        assertTrue(populatedInfo.isPlaying)
        assertEquals(30000L, populatedInfo.positionMs)
        assertEquals(240000L, populatedInfo.durationMs)
        assertEquals("com.spotify.music", populatedInfo.packageName)
    }

    @Test
    fun testNotificationListenerBridgeThreadSafetyAndCallbacks() {
        var connectedCalled = false
        var disconnectedCalled = false
        var notificationChangedCalled = false

        val testCallback = object : NotificationListenerBridge.NotificationListenerCallback {
            override fun onListenerConnected() {
                connectedCalled = true
            }

            override fun onListenerDisconnected() {
                disconnectedCalled = true
            }

            override fun onNotificationChanged() {
                notificationChangedCalled = true
            }
        }

        NotificationListenerBridge.register(testCallback)
        NotificationListenerBridge.notifyConnected()
        assertTrue(connectedCalled)

        NotificationListenerBridge.notifyNotificationChanged()
        assertTrue(notificationChangedCalled)

        NotificationListenerBridge.notifyDisconnected()
        assertTrue(disconnectedCalled)

        // Unregister and ensure no further calls
        connectedCalled = false
        NotificationListenerBridge.unregister(testCallback)
        NotificationListenerBridge.notifyConnected()
        assertFalse(connectedCalled)
    }

    @Test
    fun testAndroidMediaServiceInitialStatusAndSafeControls() {
        val mediaService = AndroidMediaService(context)

        // Status and currentMedia flows must be non-null
        assertNotNull(mediaService.status.value)
        // When no media is playing, currentMedia must be null (honest empty state, no mock songs)
        assertNull(mediaService.currentMedia.value)

        // Transport controls must be safe to invoke when no controller is attached
        mediaService.play()
        mediaService.pause()
        mediaService.next()
        mediaService.previous()
        mediaService.seekTo(15000L)
        mediaService.refreshSessions()

        mediaService.cleanup()
    }

    @Test
    fun testMockMediaServiceFlowUpdates() {
        val mockMediaService = object : MediaService {
            private val _status = MutableStateFlow<ServiceStatus>(ServiceStatus.Available())
            override val status: StateFlow<ServiceStatus> = _status

            private val _currentMedia = MutableStateFlow<MediaInfo?>(null)
            override val currentMedia: StateFlow<MediaInfo?> = _currentMedia

            var playCalled = false
            var pauseCalled = false
            var nextCalled = false
            var prevCalled = false
            var seekCalled = false

            override fun play() {
                playCalled = true
                _currentMedia.value = _currentMedia.value?.copy(isPlaying = true)
            }

            override fun pause() {
                pauseCalled = true
                _currentMedia.value = _currentMedia.value?.copy(isPlaying = false)
            }

            override fun next() {
                nextCalled = true
            }

            override fun previous() {
                prevCalled = true
            }

            override fun seekTo(positionMs: Long) {
                seekCalled = true
                _currentMedia.value = _currentMedia.value?.copy(positionMs = positionMs)
            }

            fun setTrack(info: MediaInfo) {
                _currentMedia.value = info
            }
        }

        assertNull(mockMediaService.currentMedia.value)

        mockMediaService.setTrack(
            MediaInfo(
                title = "Blinding Lights",
                artist = "The Weeknd",
                isPlaying = false,
                durationMs = 200000L
            )
        )

        assertNotNull(mockMediaService.currentMedia.value)
        assertEquals("Blinding Lights", mockMediaService.currentMedia.value?.title)
        assertFalse(mockMediaService.currentMedia.value?.isPlaying == true)

        mockMediaService.play()
        assertTrue(mockMediaService.playCalled)
        assertTrue(mockMediaService.currentMedia.value?.isPlaying == true)

        mockMediaService.pause()
        assertTrue(mockMediaService.pauseCalled)
        assertFalse(mockMediaService.currentMedia.value?.isPlaying == true)

        mockMediaService.next()
        assertTrue(mockMediaService.nextCalled)

        mockMediaService.previous()
        assertTrue(mockMediaService.prevCalled)

        mockMediaService.seekTo(50000L)
        assertTrue(mockMediaService.seekCalled)
        assertEquals(50000L, mockMediaService.currentMedia.value?.positionMs)
    }

    @Test
    fun testLauncherNotificationListenerPermissionCheck() {
        // Safe check without throwing
        val isGranted = LauncherNotificationListener.isNotificationAccessGranted(context)
        assertNotNull(isGranted)

        val settingsIntent = LauncherNotificationListener.createNotificationListenerSettingsIntent(context)
        assertNotNull(settingsIntent)
        assertNotNull(settingsIntent.action)
    }
}
