package com.taskease.collegeapp.Constants

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val notificationHelper by lazy { NotificationHelper(applicationContext) }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM message here
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "Default Title"
            val message = it.body ?: "Default message"

            // Show notification
            notificationHelper.showNotification(title, message)
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }

        // Handle data payload if needed
        remoteMessage.data.let {
            Log.d("FCM", "Message data payload: $it")
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        // Send token to your server if needed
    }
}
