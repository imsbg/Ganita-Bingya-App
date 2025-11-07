// FILE: app/src/main/java/com/sandeep/ganitabigyan/PuzzleStateRepository.kt
package com.sandeep.ganitabigyan

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define a DataStore instance for the app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "puzzle_state")

/**
 * A class to manage saving and loading the puzzle game state.
 */
class PuzzleStateRepository(context: Context) {

    private val dataStore = context.dataStore

    // Define keys for each piece of data we want to save
    companion object {
        private val TILES_KEY = stringPreferencesKey("puzzle_tiles")
        private val GRID_SIZE_KEY = intPreferencesKey("puzzle_grid_size")
        private val MOVES_KEY = intPreferencesKey("puzzle_moves")
        private val TIME_KEY = intPreferencesKey("puzzle_time")
    }

    /**
     * Data class to hold all parts of the game state together.
     */
    data class PuzzleState(
        val tiles: List<Int>,
        val gridSize: Int,
        val moves: Int,
        val timeElapsedSec: Int
    )

    /**
     * A public flow that emits the saved game state whenever it changes.
     * It emits null if no game is saved.
     */
    val savedPuzzleState: Flow<PuzzleState?> = dataStore.data.map { preferences ->
        // Retrieve all saved data
        val gridSize = preferences[GRID_SIZE_KEY]
        val tilesString = preferences[TILES_KEY]
        val moves = preferences[MOVES_KEY]
        val time = preferences[TIME_KEY]

        // If gridSize or tiles are missing, there's no valid saved game
        if (gridSize == null || tilesString == null || moves == null || time == null) {
            null
        } else {
            // Convert the tile string "1,2,3..." back into a list of integers
            val tiles = tilesString.split(",").mapNotNull { it.toIntOrNull() }
            PuzzleState(
                tiles = tiles,
                gridSize = gridSize,
                moves = moves,
                timeElapsedSec = time
            )
        }
    }

    /**
     * Saves the current game state.
     */
    suspend fun saveState(state: PuzzleState) {
        dataStore.edit { preferences ->
            // Convert the list of tiles into a single string "1,2,3..." for storage
            preferences[TILES_KEY] = state.tiles.joinToString(",")
            preferences[GRID_SIZE_KEY] = state.gridSize
            preferences[MOVES_KEY] = state.moves
            preferences[TIME_KEY] = state.timeElapsedSec
        }
    }

    /**
     * Deletes the saved game state. We call this when a game is finished or abandoned.
     */
    suspend fun clearState() {
        dataStore.edit { preferences ->
            preferences.remove(TILES_KEY)
            preferences.remove(GRID_SIZE_KEY)
            preferences.remove(MOVES_KEY)
            preferences.remove(TIME_KEY)
        }
    }
}