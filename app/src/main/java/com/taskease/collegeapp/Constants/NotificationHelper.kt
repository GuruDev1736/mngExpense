package com.taskease.collegeapp.Constants

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.taskease.collegeapp.R

class NotificationHelper(private val context: Context) {

    private val channelId = "fcm_default_channel"
    private val channelName = "FCM Default Channel"

    init {
        // Create notification channel for devices running Android Oreo (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.college)  // Use your app's notification icon
            .setAutoCancel(true)  // Automatically dismiss the notification when tapped
            .build()

        // Unique notification ID to prevent overwriting
        val notificationId = System.currentTimeMillis().toInt()

        // Display the notification
        notificationManager.notify(notificationId, notification)
    }
}