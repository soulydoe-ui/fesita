package com.fiestast.launcher.android.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread-safe notification and connection bridge between LauncherNotificationListener
 * and automotive background services (MediaSession, Turn-by-Turn navigation).
 */
object NotificationListenerBridge {

    interface NotificationListenerCallback {
        fun onListenerConnected() {}
        fun onListenerDisconnected() {}
        fun onNotificationChanged() {}
        fun onNotificationPosted(sbn: StatusBarNotification) {
            onNotificationChanged()
        }
        fun onNotificationRemoved(sbn: StatusBarNotification) {
            onNotificationChanged()
        }
    }

    private val callbacks = CopyOnWriteArrayList<NotificationListenerCallback>()
    private var activeServiceRef: WeakReference<NotificationListenerService>? = null

    fun setActiveService(service: NotificationListenerService?) {
        activeServiceRef = if (service != null) WeakReference(service) else null
    }

    fun getActiveNotifications(): Array<StatusBarNotification>? {
        return try {
            activeServiceRef?.get()?.activeNotifications
        } catch (_: Throwable) {
            null
        }
    }

    fun register(callback: NotificationListenerCallback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    fun unregister(callback: NotificationListenerCallback) {
        callbacks.remove(callback)
    }

    fun notifyConnected() {
        for (cb in callbacks) {
            try {
                cb.onListenerConnected()
            } catch (_: Throwable) {}
        }
    }

    fun notifyDisconnected() {
        for (cb in callbacks) {
            try {
                cb.onListenerDisconnected()
            } catch (_: Throwable) {}
        }
    }

    fun notifyNotificationChanged() {
        for (cb in callbacks) {
            try {
                cb.onNotificationChanged()
            } catch (_: Throwable) {}
        }
    }

    fun notifyNotificationPosted(sbn: StatusBarNotification) {
        for (cb in callbacks) {
            try {
                cb.onNotificationPosted(sbn)
            } catch (_: Throwable) {}
        }
    }

    fun notifyNotificationRemoved(sbn: StatusBarNotification) {
        for (cb in callbacks) {
            try {
                cb.onNotificationRemoved(sbn)
            } catch (_: Throwable) {}
        }
    }
}
