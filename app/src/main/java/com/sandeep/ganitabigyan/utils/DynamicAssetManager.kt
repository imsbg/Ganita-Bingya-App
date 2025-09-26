// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/DynamicAssetManager.kt
// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE

package com.sandeep.ganitabigyan.utils

import android.content.Context
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

// <<< UPDATED SplashConfig to include the new image path >>>
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

    suspend fun checkForUpdates() {
        withContext(Dispatchers.IO) {
            try {
                val urlWithCacheBuster = "$REMOTE_CONFIG_URL?t=${System.currentTimeMillis()}"
                val configJson = URL(urlWithCacheBuster).readText()
                val jsonObject = JSONObject(configJson)

                // Check for new logo
                val remoteVersion = jsonObject.getInt("logo_version")
                val localVersion = dataStore.dynamicAssetVersion.first()
                var newLogoPath = dataStore.dynamicLogoPath.first()
                if (remoteVersion > localVersion || !File(newLogoPath).exists()) {
                    val logoUrl = jsonObject.getString("logo_url")
                    newLogoPath = if (logoUrl.isNotBlank()) downloadAndSaveAsset(logoUrl, "dynamic_logo.png") ?: "" else { deleteAsset("dynamic_logo.png"); "" }
                }

                // <<< NEW: Check for new background image >>>
                val bgImageJson = jsonObject.optJSONObject("background_image")
                var newBgImagePath = ""
                var newBgImageVersion = 0
                if (bgImageJson != null) {
                    newBgImageVersion = bgImageJson.getInt("version")
                    val localBgImageVersion = dataStore.dynamicBackgroundImageVersion.first()
                    newBgImagePath = dataStore.dynamicBackgroundImagePath.first()

                    if (newBgImageVersion > localBgImageVersion || !File(newBgImagePath).exists()) {
                        val bgImageUrl = bgImageJson.getString("url")
                        newBgImagePath = downloadAndSaveAsset(bgImageUrl, "dynamic_background.png") ?: ""
                    }
                } else {
                    // If no background image in JSON, delete the old one from the phone
                    deleteAsset("dynamic_background.png")
                    newBgImagePath = ""
                }

                // Save all assets
                dataStore.saveDynamicAssets(
                    version = remoteVersion,
                    logoPath = newLogoPath,
                    backgroundJson = jsonObject.getJSONObject("splash_background").toString(),
                    textColorHex = jsonObject.getString("text_color"),
                    splashTextsJson = jsonObject.getJSONObject("texts").toString(),
                    bgImageVersion = newBgImageVersion,
                    bgImagePath = newBgImagePath
                )

            } catch (e: Exception) {
                e.printStackTrace()
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

        // <<< NEW: Get the background image path >>>
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
                val file = File(context.filesDir, filename)
                val bytes = URL(url).readBytes()
                file.writeBytes(bytes)
                file.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun deleteAsset(filename: String) {
        try {
            val file = File(context.filesDir, filename)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) { /* Do nothing */ }
    }
}