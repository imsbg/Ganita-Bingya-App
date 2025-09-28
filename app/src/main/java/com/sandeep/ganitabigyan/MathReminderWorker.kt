// FILE: app/src/main/java/com/sandeep/ganitabigyan/MathReminderWorker.kt
// PASTE THIS ENTIRE, FINAL CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class MathReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dataStore = SettingsDataStore(applicationContext)
        val remindersAreEnabled = dataStore.areRemindersEnabled.first()

        // Only proceed if the setting is turned ON.
        if (remindersAreEnabled) {
            sendNotification()
            // <<< THE FIX IS HERE: We REMOVED the line "scheduleReminders(applicationContext)" >>>
            // The WorkManager is already periodic. It knows to run again tomorrow.
            // We do NOT need to schedule it again from here.
        }

        return Result.success()
    }

    private fun sendNotification() {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "math_reminder_channel"

        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.reminder_sound}")
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
            setSound(soundUri, audioAttributes)
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}