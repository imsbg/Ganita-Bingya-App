// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/DynamicAssetManager.kt
// VERSION: FINAL - Uses the professional GitHub Pages URL.

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
import kotlin.random.Random

data class SplashConfig(
    val logoPath: String?,
    val backgroundBrush: Brush,
    val textColor: Color,
    val splashText1: String,
    val splashText2: String,
    val splashText3: String
)

class DynamicAssetManager(private val context: Context) {
    private val dataStore = SettingsDataStore(context)

    // <<< THIS IS THE ONLY CHANGE NEEDED >>>
    // We now use your new, professional, and permanent GitHub Pages URL.
    private val REMOTE_CONFIG_URL = "https://imsbg.github.io/Ganita-Bingya-App/logo_config.json"

    suspend fun checkForUpdates() {
        withContext(Dispatchers.IO) {
            try {
                val urlWithCacheBuster = "$REMOTE_CONFIG_URL?t=${System.currentTimeMillis()}"
                val configJson = URL(urlWithCacheBuster).readText()
                val jsonObject = JSONObject(configJson)
                val remoteVersion = jsonObject.getInt("logo_version")
                val localVersion = dataStore.dynamicAssetVersion.first()

                if (remoteVersion > localVersion) {
                    val logoUrl = jsonObject.getString("logo_url")
                    val backgroundJson = jsonObject.getJSONObject("splash_background")
                    val textColorHex = jsonObject.getString("text_color")
                    val textsJson = jsonObject.getJSONObject("texts")
                    val newLogoPath = if (logoUrl.isNotBlank()) downloadAndSaveImage(logoUrl) else { deleteSavedImage(); null }

                    dataStore.saveDynamicAssets(
                        version = remoteVersion,
                        logoPath = newLogoPath ?: "",
                        backgroundJson = backgroundJson.toString(),
                        textColorHex = textColorHex,
                        splashTextsJson = textsJson.toString()
                    )
                }
            } catch (e: Exception) { e.printStackTrace() }
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

        return SplashConfig(logoPath, backgroundBrush, textColor, splashText1, splashText2, splashText3)
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

    private fun downloadAndSaveImage(url: String): String? { return try { val file = File(context.filesDir, "dynamic_logo.png"); val bytes = URL(url).readBytes(); file.writeBytes(bytes); file.absolutePath } catch (e: Exception) { null } }
    private fun deleteSavedImage() { try { val file = File(context.filesDir, "dynamic_logo.png"); if (file.exists()) { file.delete() } } catch (e: Exception) { } }
}