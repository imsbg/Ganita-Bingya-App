package com.sandeep.ganitabigyan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MathReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "math_reminder_channel"

        // Create notification channel for Android O+
        val channel = NotificationChannel(
            channelId,
            "Math Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for daily math practice reminders"
        }
        notificationManager.createNotificationChannel(channel)

        // Intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use your app's icon
            .setContentTitle("ଗଣିତ ବିଜ୍ଞ")
            .setContentText("ଆଜି ଗଣିତ ପଢିବ ନା ନାହିଁ? ମୁଁ ଅପେକ୍ଷା କରିଛି")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}