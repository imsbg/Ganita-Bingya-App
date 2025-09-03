// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsDataStore.kt
// VERSION: FINAL, CORRECTED - Restores original variable names to fix all errors.

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        // --- These keys remain the same ---
        private val WELCOME_COMPLETED_KEY = booleanPreferencesKey("welcome_completed")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val GAME_TYPE_KEY = stringPreferencesKey("game_type")
        private val DIFFICULTY_LEVEL_KEY = stringPreferencesKey("difficulty_level")
        private val AUTO_SCROLL_KEY = booleanPreferencesKey("auto_scroll")
        // Keys for reminders
        private val MORNING_REMINDER_TIME_KEY = stringPreferencesKey("morning_reminder_time")
        private val EVENING_REMINDER_TIME_KEY = stringPreferencesKey("evening_reminder_time")
    }

    // <<< FIX 1: The variable name is changed back to hasCompletedWelcome >>>
    val hasCompletedWelcome: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[WELCOME_COMPLETED_KEY] ?: false
        }

    suspend fun setWelcomeCompleted() {
        context.dataStore.edit { settings ->
            settings[WELCOME_COMPLETED_KEY] = true
        }
    }

    val language: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: Locale.getDefault().language
        }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = languageCode
        }
    }

    val isVibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[VIBRATION_KEY] ?: true
        }

    // <<< FIX 2: The function name is changed back to setVibrationEnabled >>>
    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[VIBRATION_KEY] = enabled
        }
    }

    val gameType: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[GAME_TYPE_KEY] ?: "game_type_mixed"
        }

    val difficultyLevel: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[DIFFICULTY_LEVEL_KEY] ?: "difficulty_easy"
        }

    suspend fun saveSettings(typeKey: String, levelKey: String) {
        context.dataStore.edit { settings ->
            settings[GAME_TYPE_KEY] = typeKey
            settings[DIFFICULTY_LEVEL_KEY] = levelKey
        }
    }

    val autoScroll: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[AUTO_SCROLL_KEY] ?: false
        }

    suspend fun saveAutoScroll(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[AUTO_SCROLL_KEY] = enabled
        }
    }

    // <<< FIX 3: All reminder variable and function names are restored >>>
    val morningReminderTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[MORNING_REMINDER_TIME_KEY] ?: "09:00" // Default to 9 AM
        }

    suspend fun setMorningReminderTime(time: String) {
        context.dataStore.edit { settings ->
            settings[MORNING_REMINDER_TIME_KEY] = time
        }
    }

    val eveningReminderTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[EVENING_REMINDER_TIME_KEY] ?: "19:00" // Default to 7 PM
        }

    suspend fun setEveningReminderTime(time: String) {
        context.dataStore.edit { settings ->
            settings[EVENING_REMINDER_TIME_KEY] = time
        }
    }
}