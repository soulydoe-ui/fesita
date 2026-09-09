package com.fiestast.launcher.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Handles ACTION_BOOT_COMPLETED to ensure safe initialization upon system startup.
 * Strictly avoids launching unwanted activities or interfering with system boot.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Fiesta ST Launcher received ACTION_BOOT_COMPLETED. Subsystems ready.")
        }
    }

    companion object {
        private const val TAG = "FiestaSTBoot"
    }
}
