package com.fiestast.launcher

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.fiestast.launcher.android.bluetooth.AndroidBluetoothService
import com.fiestast.launcher.android.notifications.LauncherNotificationListener
import com.fiestast.launcher.android.receivers.BootCompletedReceiver
import com.fiestast.launcher.android.receivers.SystemStateReceiver
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase1CoreFoundationsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testBluetoothServicePermissionsAndAdapterSafety() {
        val bluetoothService = AndroidBluetoothService(context)
        // Refreshing state must be safe without throwing SecurityException
        bluetoothService.refreshState()
        assertNotNull(bluetoothService.status.value)
        assertNotNull(bluetoothService.isBluetoothEnabled.value)
    }

    @Test
    fun testNotificationListenerServiceArchitecture() {
        // Must be able to check notification access without crashing
        val isGranted = LauncherNotificationListener.isNotificationAccessGranted(context)
        // Check settings intent generation
        val intent = LauncherNotificationListener.createNotificationListenerSettingsIntent(context)
        assertNotNull(intent)
        assertNotNull(intent.action)
    }

    @Test
    fun testBootCompletedReceiverInitialization() {
        val receiver = BootCompletedReceiver()
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        // Must receive boot broadcast cleanly without crashing or launching unauthorized tasks
        receiver.onReceive(context, intent)
    }

    @Test
    fun testSystemStateReceiverTimeAndPower() {
        var timeChangedTriggered = false
        val receiver = SystemStateReceiver(
            onTimeOrTimezoneChanged = { timeChangedTriggered = true }
        )

        receiver.register(context)

        // Time changed simulation
        val timeIntent = Intent(Intent.ACTION_TIME_CHANGED)
        receiver.onReceive(context, timeIntent)
        assertTrue(timeChangedTriggered)

        // Power connected simulation
        val powerIntent = Intent(Intent.ACTION_POWER_CONNECTED)
        receiver.onReceive(context, powerIntent)
        assertTrue(receiver.powerState.value.isPowerConnected)

        // Power disconnected simulation
        val powerDisconnectIntent = Intent(Intent.ACTION_POWER_DISCONNECTED)
        receiver.onReceive(context, powerDisconnectIntent)
        assertTrue(!receiver.powerState.value.isPowerConnected)

        receiver.unregister(context)
    }
}
