// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsDataStore.kt
// PASTE THIS ENTIRE, CORRECTED CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DarkMode {
    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"
}

class SettingsDataStore(private val context: Context) {

    companion object {
        private val WELCOME_COMPLETED_KEY = booleanPreferencesKey("welcome_completed")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val SOUND_KEY = booleanPreferencesKey("sound_enabled")
        private val MORNING_REMINDER_TIME_KEY = stringPreferencesKey("morning_reminder_time")
        private val EVENING_REMINDER_TIME_KEY = stringPreferencesKey("evening_reminder_time")
        private val IGNORED_UPDATE_VERSION_KEY = stringPreferencesKey("ignored_update_version")
        private val DARK_MODE_KEY = stringPreferencesKey("dark_mode_preference")
        private val REMINDERS_ENABLED_KEY = booleanPreferencesKey("reminders_enabled")

        // <<< FIX: Adding the missing keys back in >>>
        private val GAME_TYPE_KEY = stringPreferencesKey("game_type")
        private val DIFFICULTY_LEVEL_KEY = stringPreferencesKey("difficulty_level")
        private val AUTO_SCROLL_KEY = booleanPreferencesKey("auto_scroll")

        // Dynamic Splash Keys
        private val DYNAMIC_ASSET_VERSION_KEY = intPreferencesKey("dynamic_asset_version")
        private val DYNAMIC_LOGO_PATH_KEY = stringPreferencesKey("dynamic_logo_path")
        private val DYNAMIC_BACKGROUND_JSON_KEY = stringPreferencesKey("dynamic_background_json")
        private val DYNAMIC_TEXT_COLOR_KEY = stringPreferencesKey("dynamic_text_color")
        private val DYNAMIC_SPLASH_TEXTS_JSON_KEY = stringPreferencesKey("dynamic_splash_texts_json")
        private val DYNAMIC_BACKGROUND_IMAGE_VERSION_KEY = intPreferencesKey("dynamic_background_image_version")
        private val DYNAMIC_BACKGROUND_IMAGE_PATH_KEY = stringPreferencesKey("dynamic_background_image_path")
    }

    val hasCompletedWelcome: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[WELCOME_COMPLETED_KEY] ?: false }
    suspend fun setWelcomeCompleted() { context.dataStore.edit { settings -> settings[WELCOME_COMPLETED_KEY] = true } }
    val language: Flow<String> = context.dataStore.data.map { preferences -> preferences[LANGUAGE_KEY] ?: "system" }
    suspend fun saveLanguage(languageCode: String) { context.dataStore.edit { settings -> settings[LANGUAGE_KEY] = languageCode } }
    val isVibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[VIBRATION_KEY] ?: true }
    suspend fun setVibrationEnabled(enabled: Boolean) { context.dataStore.edit { settings -> settings[VIBRATION_KEY] = enabled } }
    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[SOUND_KEY] ?: true }
    suspend fun setSoundEnabled(enabled: Boolean) { context.dataStore.edit { settings -> settings[SOUND_KEY] = enabled } }
    val morningReminderTime: Flow<String> = context.dataStore.data.map { preferences -> preferences[MORNING_REMINDER_TIME_KEY] ?: "07:00" }
    suspend fun setMorningReminderTime(time: String) { context.dataStore.edit { settings -> settings[MORNING_REMINDER_TIME_KEY] = time } }
    val eveningReminderTime: Flow<String> = context.dataStore.data.map { preferences -> preferences[EVENING_REMINDER_TIME_KEY] ?: "19:00" }
    suspend fun setEveningReminderTime(time: String) { context.dataStore.edit { settings -> settings[EVENING_REMINDER_TIME_KEY] = time } }
    val ignoredUpdateVersion: Flow<String> = context.dataStore.data.map { preferences -> preferences[IGNORED_UPDATE_VERSION_KEY] ?: "" }
    suspend fun setIgnoredUpdateVersion(version: String) { context.dataStore.edit { settings -> settings[IGNORED_UPDATE_VERSION_KEY] = version } }
    val darkModePreference: Flow<String> = context.dataStore.data.map { preferences -> preferences[DARK_MODE_KEY] ?: DarkMode.SYSTEM }
    suspend fun setDarkModePreference(mode: String) { context.dataStore.edit { settings -> settings[DARK_MODE_KEY] = mode } }
    val areRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[REMINDERS_ENABLED_KEY] ?: true }
    suspend fun setRemindersEnabled(enabled: Boolean) { context.dataStore.edit { settings -> settings[REMINDERS_ENABLED_KEY] = enabled } }

    // <<< FIX: Adding the missing flows and functions back in >>>
    val gameType: Flow<String> = context.dataStore.data.map { preferences -> preferences[GAME_TYPE_KEY] ?: "game_type_mixed" }
    val difficultyLevel: Flow<String> = context.dataStore.data.map { preferences -> preferences[DIFFICULTY_LEVEL_KEY] ?: "difficulty_easy" }
    suspend fun saveSettings(typeKey: String, levelKey: String) { context.dataStore.edit { settings -> settings[GAME_TYPE_KEY] = typeKey; settings[DIFFICULTY_LEVEL_KEY] = levelKey } }
    val autoScroll: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[AUTO_SCROLL_KEY] ?: false }
    suspend fun saveAutoScroll(enabled: Boolean) { context.dataStore.edit { settings -> settings[AUTO_SCROLL_KEY] = enabled } }


    // --- Dynamic Asset Flows and Functions ---
    val dynamicAssetVersion: Flow<Int> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_ASSET_VERSION_KEY] ?: 0 }
    val dynamicLogoPath: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_LOGO_PATH_KEY] ?: "" }
    val dynamicBackgroundJson: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_BACKGROUND_JSON_KEY] ?: "{ \"type\": \"solid\", \"colors\": [\"#FFFFFF\"] }" }
    val dynamicTextColor: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_TEXT_COLOR_KEY] ?: "#000000" }
    val dynamicSplashTextsJson: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_SPLASH_TEXTS_JSON_KEY] ?: "{}" }
    val dynamicBackgroundImageVersion: Flow<Int> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_BACKGROUND_IMAGE_VERSION_KEY] ?: 0 }
    val dynamicBackgroundImagePath: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_BACKGROUND_IMAGE_PATH_KEY] ?: "" }

    suspend fun saveDynamicAssets(
        version: Int,
        logoPath: String,
        backgroundJson: String,
        textColorHex: String,
        splashTextsJson: String,
        bgImageVersion: Int,
        bgImagePath: String
    ) {
        context.dataStore.edit { settings ->
            settings[DYNAMIC_ASSET_VERSION_KEY] = version
            settings[DYNAMIC_LOGO_PATH_KEY] = logoPath
            settings[DYNAMIC_BACKGROUND_JSON_KEY] = backgroundJson
            settings[DYNAMIC_TEXT_COLOR_KEY] = textColorHex
            settings[DYNAMIC_SPLASH_TEXTS_JSON_KEY] = splashTextsJson
            settings[DYNAMIC_BACKGROUND_IMAGE_VERSION_KEY] = bgImageVersion
            settings[DYNAMIC_BACKGROUND_IMAGE_PATH_KEY] = bgImagePath
        }
    }
}