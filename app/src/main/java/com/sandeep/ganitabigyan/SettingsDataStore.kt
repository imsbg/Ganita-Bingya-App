// SettingsDataStore.kt

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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        val KEY_GAME_TYPE = stringPreferencesKey("game_type")
        val KEY_DIFFICULTY_LEVEL = stringPreferencesKey("difficulty_level")
        val KEY_AUTO_SCROLL = booleanPreferencesKey("auto_scroll_enabled")
        // NEW SETTINGS
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_MORNING_REMINDER = stringPreferencesKey("morning_reminder_time")
        val KEY_EVENING_REMINDER = stringPreferencesKey("evening_reminder_time")
    }

    val gameType: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_GAME_TYPE] ?: "ମିଶ୍ରଣ"
    }

    val difficultyLevel: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_DIFFICULTY_LEVEL] ?: "ସହଜ"
    }

    val autoScroll: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SCROLL] ?: false
    }

    // NEW FLOWS
    val isVibrationEnabled: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_VIBRATION_ENABLED] ?: true // Default to ON
    }

    val morningReminderTime: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_MORNING_REMINDER] ?: "08:00" // Default to 8 AM
    }

    val eveningReminderTime: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_EVENING_REMINDER] ?: "20:00" // Default to 8 PM
    }

    suspend fun saveSettings(gameType: String, difficultyLevel: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_GAME_TYPE] = gameType
            preferences[KEY_DIFFICULTY_LEVEL] = difficultyLevel
        }
    }

    suspend fun saveAutoScroll(isEnabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_AUTO_SCROLL] = isEnabled
        }
    }

    // NEW SAVE FUNCTIONS
    suspend fun setVibrationEnabled(isEnabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_VIBRATION_ENABLED] = isEnabled
        }
    }

    suspend fun setMorningReminderTime(time: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_MORNING_REMINDER] = time
        }
    }

    suspend fun setEveningReminderTime(time: String) {
        appContext.dataStore.edit { preferences ->
            preferences[KEY_EVENING_REMINDER] = time
        }
    }
}