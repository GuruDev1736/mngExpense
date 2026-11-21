package com.guruprasad.mngexpense.data.backup

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.guruprasad.mngexpense.MainActivity
import com.guruprasad.mngexpense.R

/** "Backup failed" — the scheduled backup must not fail silently. */
object BackupNotifier {
    private const val CHANNEL_ID = "backups"
    private const val NOTIF_ID = 400

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Backups", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Tells you when a scheduled backup couldn't be saved" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // checked with areNotificationsEnabled()
    fun notifyFailed(context: Context, message: String) {
        val nm = NotificationManagerCompat.from(context)
        if (!nm.areNotificationsEnabled()) return
        createChannel(context)
        val open = PendingIntent.getActivity(
            context, 2, MainActivity.backupIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_backup)
            .setContentTitle("Backup failed")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID, notification)
    }

    fun cancelFailed(context: Context) = NotificationManagerCompat.from(context).cancel(NOTIF_ID)
}
