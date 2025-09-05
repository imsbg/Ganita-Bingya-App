// FILE: app/src/main/java/com/sandeep/ganitabigyan/DynamicAssetWorker.kt

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sandeep.ganitabigyan.utils.DynamicAssetManager

class DynamicAssetWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Create an instance of our asset manager and check for updates
            val assetManager = DynamicAssetManager(applicationContext)
            assetManager.checkForUpdates()
            // Indicate that the work was successful
            Result.success()
        } catch (e: Exception) {
            // If there's an error (e.g., no internet), we can retry later
            Result.retry()
        }
    }
}