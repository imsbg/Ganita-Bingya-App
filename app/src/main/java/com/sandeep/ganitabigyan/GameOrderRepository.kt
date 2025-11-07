// FILE: app/src/main/java/com/sandeep/ganitabigyan/GameOrderRepository.kt
package com.sandeep.ganitabigyan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameOrderDataStore: DataStore<Preferences> by preferencesDataStore(name = "game_order")

class GameOrderRepository(context: Context) {

    private val dataStore = context.gameOrderDataStore

    companion object {
        private val GAME_ORDER_KEY = stringPreferencesKey("game_order_key")
    }

    val gameOrderFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        val savedOrderString = preferences[GAME_ORDER_KEY] ?: ""
        if (savedOrderString.isBlank()) {
            emptyList()
        } else {
            savedOrderString.split(",")
        }
    }

    suspend fun saveOrder(orderedRoutes: List<String>) {
        dataStore.edit { preferences ->
            preferences[GAME_ORDER_KEY] = orderedRoutes.joinToString(",")
        }
    }

    /**
     * <<< ADD THIS NEW FUNCTION TO CLEAR THE SAVED ORDER >>>
     */
    suspend fun clearOrder() {
        dataStore.edit { preferences ->
            preferences.remove(GAME_ORDER_KEY)
        }
    }
}