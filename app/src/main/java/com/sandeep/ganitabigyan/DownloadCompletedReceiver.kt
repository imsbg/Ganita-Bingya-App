// FILE: app/src/main/java/com/sandeep/ganitabigyan/DownloadCompletedReceiver.kt

package com.sandeep.ganitabigyan

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * BroadcastReceiver triggered by the system when the DownloadManager finishes a file download.
 * Its main job is to prompt the user to install the downloaded APK file.
 */
class DownloadCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Only act on the ACTION_DOWNLOAD_COMPLETE event
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {

            // Get the ID of the completed download
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == -1L) {
                return
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            // Get the URI (address) of the downloaded file
            val fileUri = downloadManager.getUriForDownloadedFile(id)

            if (fileUri != null) {
                // If the file URI was found, create an installation intent
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    // Launch the intent to start the installation process
                    context.startActivity(installIntent)
                } catch (e: Exception) {
                    // If any error occurs, show a toast message
                    Toast.makeText(context, R.string.failed_to_start_installer, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } else {
                // If the file URI was not found, show a download failed toast message
                Toast.makeText(context, R.string.download_failed_generic, Toast.LENGTH_LONG).show()
            }
        }
    }
}