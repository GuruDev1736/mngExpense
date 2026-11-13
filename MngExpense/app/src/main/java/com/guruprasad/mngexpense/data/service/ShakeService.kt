package com.guruprasad.mngexpense.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.guruprasad.mngexpense.MainActivity
import com.guruprasad.mngexpense.R
import com.guruprasad.mngexpense.util.ShakePrefs

/**
 * Foreground service: listens to the accelerometer all the time
 * (screen on/off, locked/unlocked) and opens MainActivity on a shake.
 */
class ShakeService : Service() {

    private lateinit var sensorManager: SensorManager
    private val detector = ShakeDetector(onShake = ::onShake)

    override fun onCreate() {
        super.onCreate()
        createChannels(this)
        goForeground()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // The "wake-up" accelerometer keeps sending events while the phone sleeps
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, true)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel == null) {
            Log.e(TAG, "No accelerometer on this device")
            stopSelf()
            return
        }
        // SENSOR_DELAY_UI (~60ms) is plenty for shake detection and far cheaper than GAME.
        // No wake lock: the wake-up accelerometer already wakes the CPU to deliver events.
        sensorManager.registerListener(detector, accel, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        detector.thresholdG = ShakePrefs.thresholdG(this) // picks up sensitivity changes
        return START_STICKY
    }

    override fun onDestroy() {
        if (::sensorManager.isInitialized) sensorManager.unregisterListener(detector)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun goForeground() {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_stat_shake)
            .setContentTitle("Shake to open is on")
            .setContentText("Shake your phone to open the app")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, NOTIF_ID_SERVICE, notification, type)
    }

    private fun onShake() {
        val launch = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(EXTRA_FROM_SHAKE, true)
        }

        // Main path: "Display over other apps" lets us open the app from the background
        if (Settings.canDrawOverlays(this)) {
            try {
                startActivity(launch)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Direct start blocked, falling back to notification", e)
            }
        }
        showFullScreenFallback(launch)
    }

    /** Without the overlay permission: a full-screen / heads-up notification the user taps. */
    private fun showFullScreenFallback(launch: Intent) {
        val pi = PendingIntent.getActivity(
            this, 1, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(this, CHANNEL_LAUNCH)
            .setSmallIcon(R.drawable.ic_stat_shake)
            .setContentTitle("Shake detected")
            .setContentText("Tap to open the app")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .setTimeoutAfter(15_000)
            .build()
        try {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIF_ID_LAUNCH, n)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission missing", e)
        }
    }

    companion object {
        private const val TAG = "ShakeService"
        const val EXTRA_FROM_SHAKE = "from_shake"
        const val CHANNEL_SERVICE = "shake_service"
        const val CHANNEL_LAUNCH = "shake_launch"
        const val NOTIF_ID_SERVICE = 101
        const val NOTIF_ID_LAUNCH = 102

        fun start(ctx: Context) =
            ContextCompat.startForegroundService(ctx, Intent(ctx, ShakeService::class.java))

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ShakeService::class.java))
        }

        fun createChannels(ctx: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return // channels need API 26
            val nm = ctx.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_SERVICE, "Shake detection", NotificationManager.IMPORTANCE_LOW)
                    .apply { setShowBadge(false) }
            )
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_LAUNCH, "Open on shake", NotificationManager.IMPORTANCE_HIGH)
                    .apply { lockscreenVisibility = Notification.VISIBILITY_PUBLIC }
            )
        }
    }
}