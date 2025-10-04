// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE

package com.sandeep.ganitabigyan.utils

import android.content.Context
import android.util.Log // <<< ADD THIS IMPORT
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import com.sandeep.ganitabigyan.R
import com.sandeep.ganitabigyan.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.Locale

// Data class is unchanged
data class SplashConfig(
    val logoPath: String?,
    val backgroundBrush: Brush,
    val textColor: Color,
    val splashText1: String,
    val splashText2: String,
    val splashText3: String,
    val backgroundImagePath: String
)

class DynamicAssetManager(private val context: Context) {
    private val dataStore = SettingsDataStore(context)
    private val REMOTE_CONFIG_URL = "https://imsbg.github.io/Ganita-Bingya-App/logo_config.json"
    private val TAG = "DynamicAssetManager" // <<< ADD A TAG FOR LOGGING

    suspend fun checkForUpdates() {
        withContext(Dispatchers.IO) {
            try {
                // <<< LOGGING: Start of the process >>>
                Log.d(TAG, "Checking for updates...")
                val urlWithCacheBuster = "$REMOTE_CONFIG_URL?t=${System.currentTimeMillis()}"
                Log.d(TAG, "Fetching from URL: $urlWithCacheBuster")

                val configJson = URL(urlWithCacheBuster).readText()
                // <<< LOGGING: Successful fetch >>>
                Log.d(TAG, "Successfully fetched JSON config.")
                // Log.v(TAG, "JSON content: $configJson") // Optional: Uncomment to see the full JSON

                val jsonObject = JSONObject(configJson)

                // Check for new logo
                val remoteVersion = jsonObject.getInt("logo_version")
                val localVersion = dataStore.dynamicAssetVersion.first()
                var newLogoPath = dataStore.dynamicLogoPath.first()
                Log.d(TAG, "Logo version check: Remote is $remoteVersion, Local is $localVersion")

                if (remoteVersion > localVersion || !File(newLogoPath).exists()) {
                    Log.d(TAG, "New logo version detected or local file missing. Downloading new logo.")
                    val logoUrl = jsonObject.getString("logo_url")
                    newLogoPath = if (logoUrl.isNotBlank()) downloadAndSaveAsset(logoUrl, "dynamic_logo.png") ?: "" else { deleteAsset("dynamic_logo.png"); "" }
                } else {
                    Log.d(TAG, "Logo is up to date.")
                }

                // Check for new background image
                val bgImageJson = jsonObject.optJSONObject("background_image")
                var newBgImagePath = dataStore.dynamicBackgroundImagePath.first()
                var newBgImageVersion = 0
                val localBgImageVersion = dataStore.dynamicBackgroundImageVersion.first()

                if (bgImageJson != null) {
                    newBgImageVersion = bgImageJson.getInt("version")
                    Log.d(TAG, "BG Image version check: Remote is $newBgImageVersion, Local is $localBgImageVersion")

                    if (newBgImageVersion > localBgImageVersion || !File(newBgImagePath).exists()) {
                        Log.d(TAG, "New background image version detected or local file missing. Downloading new background.")
                        val bgImageUrl = bgImageJson.getString("url")
                        newBgImagePath = downloadAndSaveAsset(bgImageUrl, "dynamic_background.png") ?: ""
                    } else {
                        Log.d(TAG, "Background image is up to date.")
                    }
                } else {
                    Log.d(TAG, "No background image specified in remote config. Deleting local version if it exists.")
                    deleteAsset("dynamic_background.png")
                    newBgImagePath = ""
                }

                // Save all assets
                Log.d(TAG, "Saving all asset paths and versions to DataStore.")
                dataStore.saveDynamicAssets(
                    version = remoteVersion,
                    logoPath = newLogoPath,
                    backgroundJson = jsonObject.getJSONObject("splash_background").toString(),
                    textColorHex = jsonObject.getString("text_color"),
                    splashTextsJson = jsonObject.getJSONObject("texts").toString(),
                    bgImageVersion = newBgImageVersion,
                    bgImagePath = newBgImagePath
                )
                Log.d(TAG, "Update check finished successfully.")

            } catch (e: Exception) {
                // <<< CRITICAL LOGGING: This will show us the error >>>
                Log.e(TAG, "Failed to check for updates. Error: ${e.message}")
                e.printStackTrace() // This prints the full error stack trace
            }
        }
    }

    suspend fun getSplashConfig(): SplashConfig {
        val logoPath = dataStore.dynamicLogoPath.first().takeIf { it.isNotBlank() }
        val backgroundJson = dataStore.dynamicBackgroundJson.first()
        val textColorHex = dataStore.dynamicTextColor.first()
        val textsJsonString = dataStore.dynamicSplashTextsJson.first()
        val backgroundBrush = parseBackground(backgroundJson)
        val textColor = try { Color(android.graphics.Color.parseColor(textColorHex)) } catch (e: Exception) { Color.Black }
        val currentLang = dataStore.language.first().takeIf { it != "system" } ?: Locale.getDefault().language
        val textsJson = try { JSONObject(textsJsonString) } catch (e: Exception) { JSONObject() }

        val splashText1 = getLocalizedString(textsJson, "splash_text_1", currentLang, context.getString(R.string.splash_text_1))
        val splashText2 = getLocalizedString(textsJson, "splash_text_2", currentLang, context.getString(R.string.splash_text_2))
        val splashText3 = getLocalizedString(textsJson, "splash_text_3", currentLang, "")

        val backgroundImagePath = dataStore.dynamicBackgroundImagePath.first()

        return SplashConfig(logoPath, backgroundBrush, textColor, splashText1, splashText2, splashText3, backgroundImagePath)
    }

    private fun getLocalizedString(json: JSONObject, key: String, langCode: String, fallback: String): String {
        return try {
            val textObject = json.getJSONObject(key)
            textObject.optString(langCode, textObject.optString("default", fallback))
        } catch (e: Exception) { fallback }
    }

    private fun parseBackground(jsonString: String): Brush {
        return try {
            val json = JSONObject(jsonString)
            val type = json.getString("type")
            val colorsArray = json.getJSONArray("colors")
            val colors = List(colorsArray.length()) { Color(android.graphics.Color.parseColor(colorsArray.getString(it))) }
            if (type == "gradient" && colors.size >= 2) {
                Brush.verticalGradient(colors = colors)
            } else {
                SolidColor(colors.firstOrNull() ?: Color.White)
            }
        } catch (e: Exception) { SolidColor(Color.White) }
    }

    private suspend fun downloadAndSaveAsset(url: String, filename: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Downloading asset from: $url")
                val file = File(context.filesDir, filename)
                val bytes = URL(url).readBytes()
                file.writeBytes(bytes)
                Log.d(TAG, "Successfully saved asset to: ${file.absolutePath}")
                file.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download or save asset from $url. Error: ${e.message}")
                null
            }
        }
    }

    private fun deleteAsset(filename: String) {
        try {
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                if (file.delete()) {
                    Log.d(TAG, "Successfully deleted asset: $filename")
                } else {
                    Log.w(TAG, "Failed to delete asset: $filename")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting asset $filename", e)
        }
    }
}