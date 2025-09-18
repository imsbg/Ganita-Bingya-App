package com.sandeep.ganitabigyan

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * A reusable sealed class to represent the result of an update check.
 */
sealed class UpdateCheckResult {
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : UpdateCheckResult()
    object UpToDate : UpdateCheckResult()
    object Error : UpdateCheckResult()
}

/**
 * A singleton object to handle the app update checking logic.
 * This can be called from anywhere in the app.
 */
object UpdateChecker {

    suspend fun checkForUpdates(context: Context): UpdateCheckResult {
        // We run the network call on the IO dispatcher for performance.
        return withContext(Dispatchers.IO) {
            try {
                // Get the app's current version name
                val currentVersionName = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    "1.0" // A fallback in case of an error
                }

                // Get the version the user has chosen to ignore
                val dataStore = SettingsDataStore(context)
                val ignoredVersion = dataStore.ignoredUpdateVersion.first()

                // Fetch the latest release info from GitHub
                val url = URL("https://api.github.com/repos/imsbg/Ganita-Bingya-App/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                // Parse the JSON to get the version name and download URL
                val latestVersion = json.getString("tag_name").removePrefix("v")
                var apkUrl = ""
                val assets = json.getJSONArray("assets")
                if (assets.length() > 0) {
                    apkUrl = assets.getJSONObject(0).getString("browser_download_url")
                }

                // The main logic: An update is available if the latest version is
                // 1. newer than the current version, AND
                // 2. not the version the user has explicitly ignored, AND
                // 3. has a valid download URL.
                if (latestVersion > currentVersionName && latestVersion != ignoredVersion && apkUrl.isNotEmpty()) {
                    UpdateCheckResult.UpdateAvailable(latestVersion, apkUrl)
                } else {
                    UpdateCheckResult.UpToDate
                }
            } catch (e: Exception) {
                // If anything goes wrong (like no internet), return an error state.
                e.printStackTrace()
                UpdateCheckResult.Error
            }
        }
    }
}