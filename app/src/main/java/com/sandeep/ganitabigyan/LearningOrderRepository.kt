package com.sandeep.ganitabigyan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Use a different name for the DataStore instance to avoid conflicts
private val Context.learningOrderDataStore: DataStore<Preferences> by preferencesDataStore(name = "learning_order")

class LearningOrderRepository(context: Context) {

    private val dataStore = context.learningOrderDataStore

    companion object {
        // Use a different key for the learning items order
        private val LEARNING_ORDER_KEY = stringPreferencesKey("learning_order_key")
    }

    val learningOrderFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        val savedOrderString = preferences[LEARNING_ORDER_KEY] ?: ""
        if (savedOrderString.isBlank()) {
            emptyList()
        } else {
            savedOrderString.split(",")
        }
    }

    suspend fun saveOrder(orderedRoutes: List<String>) {
        dataStore.edit { preferences ->
            preferences[LEARNING_ORDER_KEY] = orderedRoutes.joinToString(",")
        }
    }

    suspend fun clearOrder() {
        dataStore.edit { preferences ->
            preferences.remove(LEARNING_ORDER_KEY)
        }
    }
}