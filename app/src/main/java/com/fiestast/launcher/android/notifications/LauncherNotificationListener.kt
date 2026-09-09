package com.fiestast.launcher.android.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Foundational NotificationListenerService architecture for the Fiesta ST launcher.
 * Prepared for subsequent Music (MediaSessionManager) and Navigation guidance extraction phases.
 */
class LauncherNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "LauncherNotificationListener connected successfully.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "LauncherNotificationListener disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Architecture ready for Phase 2 (Media) & Phase 3 (Navigation guidance)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Architecture ready for Phase 2 (Media) & Phase 3 (Navigation guidance)
    }

    companion object {
        private const val TAG = "FiestaSTNotification"

        /**
         * Checks if notification listener access is currently granted by the user.
         */
        fun isNotificationAccessGranted(context: Context): Boolean {
            return try {
                val flat = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                ) ?: ""
                val cn = ComponentName(context, LauncherNotificationListener::class.java)
                flat.contains(cn.flattenToString()) || flat.contains(cn.flattenToShortString())
            } catch (e: Throwable) {
                Log.w(TAG, "Error checking notification listener access: ${e.message}")
                false
            }
        }

        /**
         * Creates an intent targeting the system notification listener access settings.
         */
        fun createNotificationListenerSettingsIntent(context: Context): Intent {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val cn = ComponentName(context, LauncherNotificationListener::class.java)
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).apply {
                        putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, cn.flattenToString())
                    }
                } else {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                }
            } catch (_: Throwable) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
        }
    }
}
