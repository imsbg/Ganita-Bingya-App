// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsDataStore.kt
// VERSION: FINAL - Stores the entire dynamic text JSON.

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val WELCOME_COMPLETED_KEY = booleanPreferencesKey("welcome_completed")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val SOUND_KEY = booleanPreferencesKey("sound_enabled")
        private val GAME_TYPE_KEY = stringPreferencesKey("game_type")
        private val DIFFICULTY_LEVEL_KEY = stringPreferencesKey("difficulty_level")
        private val AUTO_SCROLL_KEY = booleanPreferencesKey("auto_scroll")
        private val MORNING_REMINDER_TIME_KEY = stringPreferencesKey("morning_reminder_time")
        private val EVENING_REMINDER_TIME_KEY = stringPreferencesKey("evening_reminder_time")
        private val DYNAMIC_ASSET_VERSION_KEY = intPreferencesKey("dynamic_asset_version")
        private val DYNAMIC_LOGO_PATH_KEY = stringPreferencesKey("dynamic_logo_path")
        private val DYNAMIC_BACKGROUND_JSON_KEY = stringPreferencesKey("dynamic_background_json")
        private val DYNAMIC_TEXT_COLOR_KEY = stringPreferencesKey("dynamic_text_color")
        private val DYNAMIC_SPLASH_TEXTS_JSON_KEY = stringPreferencesKey("dynamic_splash_texts_json")
    }

    val hasCompletedWelcome: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[WELCOME_COMPLETED_KEY] ?: false }
    suspend fun setWelcomeCompleted() { context.dataStore.edit { settings -> settings[WELCOME_COMPLETED_KEY] = true } }
    val language: Flow<String> = context.dataStore.data.map { preferences -> preferences[LANGUAGE_KEY] ?: Locale.getDefault().language }
    suspend fun saveLanguage(languageCode: String) { context.dataStore.edit { settings -> settings[LANGUAGE_KEY] = languageCode } }
    val isVibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[VIBRATION_KEY] ?: true }
    suspend fun setVibrationEnabled(enabled: Boolean) { context.dataStore.edit { settings -> settings[VIBRATION_KEY] = enabled } }
    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[SOUND_KEY] ?: true }
    suspend fun setSoundEnabled(enabled: Boolean) { context.dataStore.edit { settings -> settings[SOUND_KEY] = enabled } }
    val gameType: Flow<String> = context.dataStore.data.map { preferences -> preferences[GAME_TYPE_KEY] ?: "game_type_mixed" }
    val difficultyLevel: Flow<String> = context.dataStore.data.map { preferences -> preferences[DIFFICULTY_LEVEL_KEY] ?: "difficulty_easy" }
    suspend fun saveSettings(typeKey: String, levelKey: String) { context.dataStore.edit { settings -> settings[GAME_TYPE_KEY] = typeKey; settings[DIFFICULTY_LEVEL_KEY] = levelKey } }
    val autoScroll: Flow<Boolean> = context.dataStore.data.map { preferences -> preferences[AUTO_SCROLL_KEY] ?: false }
    suspend fun saveAutoScroll(enabled: Boolean) { context.dataStore.edit { settings -> settings[AUTO_SCROLL_KEY] = enabled } }
    val morningReminderTime: Flow<String> = context.dataStore.data.map { preferences -> preferences[MORNING_REMINDER_TIME_KEY] ?: "07:00" }
    suspend fun setMorningReminderTime(time: String) { context.dataStore.edit { settings -> settings[MORNING_REMINDER_TIME_KEY] = time } }
    val eveningReminderTime: Flow<String> = context.dataStore.data.map { preferences -> preferences[EVENING_REMINDER_TIME_KEY] ?: "19:00" }
    suspend fun setEveningReminderTime(time: String) { context.dataStore.edit { settings -> settings[EVENING_REMINDER_TIME_KEY] = time } }
    val dynamicAssetVersion: Flow<Int> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_ASSET_VERSION_KEY] ?: 0 }
    val dynamicLogoPath: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_LOGO_PATH_KEY] ?: "" }
    val dynamicBackgroundJson: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_BACKGROUND_JSON_KEY] ?: "{ \"type\": \"solid\", \"colors\": [\"#FFFFFF\"] }" }
    val dynamicTextColor: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_TEXT_COLOR_KEY] ?: "#000000" }
    val dynamicSplashTextsJson: Flow<String> = context.dataStore.data.map { preferences -> preferences[DYNAMIC_SPLASH_TEXTS_JSON_KEY] ?: "{}" }

    suspend fun saveDynamicAssets(version: Int, logoPath: String, backgroundJson: String, textColorHex: String, splashTextsJson: String) {
        context.dataStore.edit { settings ->
            settings[DYNAMIC_ASSET_VERSION_KEY] = version
            settings[DYNAMIC_LOGO_PATH_KEY] = logoPath
            settings[DYNAMIC_BACKGROUND_JSON_KEY] = backgroundJson
            settings[DYNAMIC_TEXT_COLOR_KEY] = textColorHex
            settings[DYNAMIC_SPLASH_TEXTS_JSON_KEY] = splashTextsJson
        }
    }
}