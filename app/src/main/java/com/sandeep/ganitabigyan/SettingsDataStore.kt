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
        val KEY_AUTO_SCROLL = booleanPreferencesKey("auto_scroll_enabled") // <-- NEW
    }

    val gameType: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_GAME_TYPE] ?: "ମିଶ୍ରଣ"
    }

    val difficultyLevel: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_DIFFICULTY_LEVEL] ?: "ସହଜ"
    }

    val autoScroll: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SCROLL] ?: false // Default to off
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
}