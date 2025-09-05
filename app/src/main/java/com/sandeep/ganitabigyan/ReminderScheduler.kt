// FILE: app/src/main/java/com/sandeep/ganitabigyan/ReminderScheduler.kt
// VERSION: Your original, reliable code.

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun scheduleReminders(context: Context) {
    val workManager = WorkManager.getInstance(context)
    val dataStore = SettingsDataStore(context)

    GlobalScope.launch(Dispatchers.IO) {
        val morningTime = dataStore.morningReminderTime.first().split(":")
        val eveningTime = dataStore.eveningReminderTime.first().split(":")

        val morningHour = morningTime[0].toInt()
        val morningMinute = morningTime[1].toInt()

        val eveningHour = eveningTime[0].toInt()
        val eveningMinute = eveningTime[1].toInt()

        // Use REPLACE policy to ensure time changes are updated
        // This cancels and replaces any existing reminders with the same name.

        // Schedule Morning Reminder
        val morningRequest = createReminderRequest(morningHour, morningMinute)
        workManager.enqueueUniquePeriodicWork(
            "morning_reminder",
            ExistingPeriodicWorkPolicy.REPLACE, // Changed from UPDATE to REPLACE for safety
            morningRequest
        )

        // Schedule Evening Reminder
        val eveningRequest = createReminderRequest(eveningHour, eveningMinute)
        workManager.enqueueUniquePeriodicWork(
            "evening_reminder",
            ExistingPeriodicWorkPolicy.REPLACE, // Changed from UPDATE to REPLACE for safety
            eveningRequest
        )
    }
}

private fun createReminderRequest(hour: Int, minute: Int): PeriodicWorkRequest {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        // If the time is already past for today, schedule it for tomorrow
        if (before(now)) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val initialDelay = target.timeInMillis - now.timeInMillis

    return PeriodicWorkRequestBuilder<MathReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        .build()
}