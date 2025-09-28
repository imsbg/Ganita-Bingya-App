// This file is located at: utils/MyFirebaseMessagingService.kt
package com.sandeep.ganitabigyan.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sandeep.ganitabigyan.MainActivity
import com.sandeep.ganitabigyan.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This function is called when a new message is received from Firebase
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // The message from Firebase will have a "notification" part
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "New Message"
            val body = notification.body ?: ""

            // We get the URL or deep link from the "click_action"
            // When you send a notification from Firebase Console, you can set this.
            val clickAction = notification.clickAction

            sendNotification(title, body, clickAction)
        }
    }

    private fun sendNotification(title: String, messageBody: String, clickAction: String?) {
        val intent: Intent

        if (!clickAction.isNullOrEmpty()) {
            // If we have a click_action (URL or deep link), create an intent to open it
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(clickAction))
        } else {
            // If there's no click_action, just open the main screen of the app
            intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground_mono) // IMPORTANT: You MUST have this icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Ganita Bingya Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    // This function is called when Firebase gives your app a new unique token
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // You could send this token to your server if you had one, but for now we just log it.
        Log.d("FCM_TOKEN", "Refreshed token: $token")
    }
}