package com.fiestast.launcher

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import com.fiestast.launcher.android.bluetooth.AndroidBluetoothService
import com.fiestast.launcher.android.phone.AndroidPhoneService
import com.fiestast.launcher.domain.model.BluetoothDeviceInfo
import com.fiestast.launcher.domain.model.BluetoothProfileType
import com.fiestast.launcher.domain.model.CallState
import com.fiestast.launcher.domain.model.PhoneCallInfo
import com.fiestast.launcher.domain.model.ServiceStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowBluetoothAdapter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase4BluetoothPhoneTest {

    private lateinit var context: Context
    private lateinit var shadowApp: ShadowApplication
    private lateinit var bluetoothService: AndroidBluetoothService
    private lateinit var phoneService: AndroidPhoneService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context as android.app.Application)

        // Setup mock activity resolvers for DIAL and CALL intents
        val dialResolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.android.dialer"
                name = "com.android.dialer.DialtactsActivity"
                applicationInfo = ApplicationInfo().apply {
                    packageName = "com.android.dialer"
                }
            }
        }
        val dialFilter = android.content.IntentFilter(Intent.ACTION_DIAL).apply {
            addDataScheme("tel")
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:1234567890")),
            dialResolveInfo
        )
        shadowOf(context.packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:5554321")),
            dialResolveInfo
        )
        shadowOf(context.packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_DIAL),
            dialResolveInfo
        )

        val callResolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = "com.android.server.telecom"
                name = "com.android.server.telecom.CallActivity"
                applicationInfo = ApplicationInfo().apply {
                    packageName = "com.android.server.telecom"
                }
            }
        }
        shadowOf(context.packageManager).addResolveInfoForIntent(
            Intent(Intent.ACTION_CALL, Uri.parse("tel:1234567890")),
            callResolveInfo
        )

        bluetoothService = AndroidBluetoothService(context)
        phoneService = AndroidPhoneService(context)
    }

    @After
    fun tearDown() {
        bluetoothService.cleanup()
        phoneService.cleanup()
    }

    // ==========================================
    // BLUETOOTH DOMAIN & SERVICE TESTS
    // ==========================================

    @Test
    fun testBluetoothDeviceInfoModel() {
        val device = BluetoothDeviceInfo(
            name = "Pixel 8 Pro",
            isConnected = true,
            supportedProfiles = setOf(BluetoothProfileType.A2DP, BluetoothProfileType.HEADSET),
            isA2dpConnected = true,
            isHeadsetConnected = true
        )

        assertEquals("Pixel 8 Pro", device.name)
        assertTrue(device.isConnected)
        assertTrue(device.isA2dpConnected)
        assertTrue(device.isHeadsetConnected)
        assertTrue(device.supportedProfiles.contains(BluetoothProfileType.A2DP))
        assertTrue(device.supportedProfiles.contains(BluetoothProfileType.HEADSET))
    }

    @Test
    fun testBluetoothAdapterStateTracking() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            val shadowAdapter = shadowOf(adapter)
            shadowAdapter.setEnabled(true)
            bluetoothService.refreshState()

            assertTrue(bluetoothService.isBluetoothEnabled.value)
            // When enabled with no devices connected, status is Available
            assertTrue(
                bluetoothService.status.value is ServiceStatus.Available ||
                bluetoothService.status.value is ServiceStatus.Unavailable
            )

            // Test turning off adapter
            shadowAdapter.setEnabled(false)
            bluetoothService.refreshState()
            assertFalse(bluetoothService.isBluetoothEnabled.value)
        }
    }

    @Test
    fun testBluetoothStateChangedBroadcast() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val shadowAdapter = if (adapter != null) shadowOf(adapter) else null

        shadowAdapter?.setEnabled(false)
        val intent = Intent(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
        }
        bluetoothService.broadcastReceiver.onReceive(context, intent)
        assertFalse(bluetoothService.isBluetoothEnabled.value)

        shadowAdapter?.setEnabled(true)
        val onIntent = Intent(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            putExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_ON)
        }
        bluetoothService.broadcastReceiver.onReceive(context, onIntent)
        assertTrue(bluetoothService.isBluetoothEnabled.value)
    }

    @Test
    fun testOpenBluetoothSettings() {
        val result = bluetoothService.openBluetoothSettings()
        assertTrue(result)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Settings.ACTION_BLUETOOTH_SETTINGS, startedIntent?.action)
        assertTrue((startedIntent?.flags ?: 0) and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun testBluetoothLifecycleCleanup() {
        // Calling cleanup multiple times should be safe and idempotent
        bluetoothService.cleanup()
        bluetoothService.cleanup()
        assertNull(bluetoothService.a2dpProfile)
        assertNull(bluetoothService.headsetProfile)
    }

    // ==========================================
    // PHONE DOMAIN & SERVICE TESTS
    // ==========================================

    @Test
    fun testPhoneCallInfoModel() {
        val idleCall = PhoneCallInfo(state = CallState.IDLE)
        assertEquals(CallState.IDLE, idleCall.state)
        assertFalse(idleCall.isRinging)
        assertFalse(idleCall.isActive)
        assertNull(idleCall.callerNumber)

        val ringingCall = PhoneCallInfo(
            state = CallState.RINGING,
            callerNumber = "123-456-7890"
        )
        assertEquals(CallState.RINGING, ringingCall.state)
        assertTrue(ringingCall.isRinging)
        assertFalse(ringingCall.isActive)
        assertEquals("123-456-7890", ringingCall.callerNumber)

        val activeCall = PhoneCallInfo(
            state = CallState.ACTIVE,
            callerNumber = "123-456-7890"
        )
        assertEquals(CallState.ACTIVE, activeCall.state)
        assertFalse(activeCall.isRinging)
        assertTrue(activeCall.isActive)
    }

    @Test
    fun testPhoneInitialStateIsIdle() {
        assertEquals(CallState.IDLE, phoneService.callState.value)
        assertFalse(phoneService.isInCall.value)
        assertNull(phoneService.activeCallContact.value)
        assertTrue(phoneService.status.value is ServiceStatus.Available)
    }

    @Test
    fun testPhoneCallStateTransitions() {
        // 1. RINGING transition
        phoneService.updateCallState(CallState.RINGING, "555-9876")
        assertEquals(CallState.RINGING, phoneService.callState.value)
        assertFalse(phoneService.isInCall.value)
        assertEquals("555-9876", phoneService.activeCallContact.value)
        assertEquals("555-9876", phoneService.currentCallInfo.value.callerNumber)
        assertEquals(ServiceStatus.Connected, phoneService.status.value)

        // 2. ACTIVE (OFFHOOK) transition
        phoneService.updateCallState(CallState.ACTIVE, null)
        assertEquals(CallState.ACTIVE, phoneService.callState.value)
        assertTrue(phoneService.isInCall.value)
        assertEquals("555-9876", phoneService.activeCallContact.value)
        assertEquals(ServiceStatus.Connected, phoneService.status.value)

        // 3. IDLE transition
        phoneService.updateCallState(CallState.IDLE, null)
        assertEquals(CallState.IDLE, phoneService.callState.value)
        assertFalse(phoneService.isInCall.value)
        assertNull(phoneService.activeCallContact.value)
        assertTrue(phoneService.status.value is ServiceStatus.Available)
    }

    @Test
    fun testPhoneBroadcastReceiverTelephonyState() {
        // Send RINGING broadcast
        val ringingIntent = Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED).apply {
            putExtra(TelephonyManager.EXTRA_STATE, TelephonyManager.EXTRA_STATE_RINGING)
            putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, "+15551234")
        }
        phoneService.broadcastReceiver.onReceive(context, ringingIntent)

        assertEquals(CallState.RINGING, phoneService.callState.value)
        assertEquals("+15551234", phoneService.activeCallContact.value)

        // Send OFFHOOK broadcast
        val offhookIntent = Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED).apply {
            putExtra(TelephonyManager.EXTRA_STATE, TelephonyManager.EXTRA_STATE_OFFHOOK)
        }
        phoneService.broadcastReceiver.onReceive(context, offhookIntent)

        assertEquals(CallState.ACTIVE, phoneService.callState.value)
        assertTrue(phoneService.isInCall.value)

        // Send IDLE broadcast
        val idleIntent = Intent(TelephonyManager.ACTION_PHONE_STATE_CHANGED).apply {
            putExtra(TelephonyManager.EXTRA_STATE, TelephonyManager.EXTRA_STATE_IDLE)
        }
        phoneService.broadcastReceiver.onReceive(context, idleIntent)

        assertEquals(CallState.IDLE, phoneService.callState.value)
        assertFalse(phoneService.isInCall.value)
    }

    @Test
    fun testBluetoothHeadsetAudioStateChangedBroadcast() {
        // EXTRA_STATE = 12 (STATE_AUDIO_CONNECTED)
        val audioConnected = Intent("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED").apply {
            putExtra("android.bluetooth.profile.extra.STATE", 12)
        }
        phoneService.broadcastReceiver.onReceive(context, audioConnected)

        assertEquals(CallState.ACTIVE, phoneService.callState.value)
        assertTrue(phoneService.isInCall.value)

        // EXTRA_STATE = 10 (STATE_AUDIO_DISCONNECTED)
        val audioDisconnected = Intent("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED").apply {
            putExtra("android.bluetooth.profile.extra.STATE", 10)
        }
        phoneService.broadcastReceiver.onReceive(context, audioDisconnected)

        assertEquals(CallState.IDLE, phoneService.callState.value)
        assertFalse(phoneService.isInCall.value)
    }

    @Test
    fun testMakeCallActionDialFallbackWhenPermissionDenied() {
        // Without CALL_PHONE permission, makeCall must safely fall back to ACTION_DIAL
        shadowApp.denyPermissions(Manifest.permission.CALL_PHONE)
        assertFalse(phoneService.isCallPermissionGranted)

        val result = phoneService.makeCall("1234567890")
        assertTrue(result)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Intent.ACTION_DIAL, startedIntent?.action)
        assertEquals("tel:1234567890", startedIntent?.dataString)
    }

    @Test
    fun testMakeCallActionCallWhenPermissionGranted() {
        // With CALL_PHONE permission granted, makeCall initiates ACTION_CALL
        shadowApp.grantPermissions(Manifest.permission.CALL_PHONE)
        assertTrue(phoneService.isCallPermissionGranted)

        val result = phoneService.makeCall("1234567890")
        assertTrue(result)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Intent.ACTION_CALL, startedIntent?.action)
        assertEquals("tel:1234567890", startedIntent?.dataString)
    }

    @Test
    fun testLaunchDialerWithAndWithoutNumber() {
        // 1. With number
        val withNum = phoneService.launchDialer("5554321")
        assertTrue(withNum)
        var intent = shadowApp.nextStartedActivity
        assertNotNull(intent)
        assertEquals(Intent.ACTION_DIAL, intent?.action)
        assertEquals("tel:5554321", intent?.dataString)

        // 2. Without number
        val withoutNum = phoneService.launchDialer(null)
        assertTrue(withoutNum)
        intent = shadowApp.nextStartedActivity
        assertNotNull(intent)
        assertEquals(Intent.ACTION_DIAL, intent?.action)
        assertNull(intent?.data)
    }

    @Test
    fun testPhoneServiceCleanup() {
        // Calling cleanup should be safe and idempotent
        phoneService.cleanup()
        phoneService.cleanup()
    }
}
