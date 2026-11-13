package com.guruprasad.mngexpense.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.guruprasad.mngexpense.data.service.ShakeService
import com.guruprasad.mngexpense.util.ShakePrefs

/** Restarts shake detection after the phone reboots or the app is updated. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            if (ShakePrefs.isEnabled(context)) {
                try {
                    ShakeService.start(context)
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Could not start ShakeService", e)
                }
            }
        }
    }
}