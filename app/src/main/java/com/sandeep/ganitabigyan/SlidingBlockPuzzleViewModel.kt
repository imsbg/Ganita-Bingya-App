// FILE: app/src/main/java/com/sandeep/ganitabigyan/SlidingBlockPuzzleViewModel.kt
package com.sandeep.ganitabigyan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class SlidingBlockPuzzleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PuzzleStateRepository(application)

    private val _uiState = MutableStateFlow(SlidingPuzzleUiState())
    val uiState: StateFlow<SlidingPuzzleUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    val hasSavedGame: Flow<Boolean> = repository.savedPuzzleState.map { it != null }

    fun continueLastGame() {
        viewModelScope.launch {
            val savedState = repository.savedPuzzleState.first()
            if (savedState != null) {
                _uiState.update {
                    it.copy(
                        selectedGridSize = savedState.gridSize,
                        gridSize = savedState.gridSize,
                        tiles = savedState.tiles,
                        moves = savedState.moves,
                        timeElapsedSec = savedState.timeElapsedSec,
                        isGameActive = true,
                        isSolved = false
                    )
                }
                startTimer()
            }
        }
    }

    fun selectGridSize(size: Int) {
        if (size == 0) {
            timerJob?.cancel()
            viewModelScope.launch { repository.clearState() }
            _uiState.update { it.copy(selectedGridSize = 0, isGameActive = false) }
            return
        }
        _uiState.update { it.copy(selectedGridSize = size) }
        startNewGame()
    }

    private fun startNewGame() {
        timerJob?.cancel()
        viewModelScope.launch { repository.clearState() }
        val size = _uiState.value.selectedGridSize
        if (size < 2) return

        val totalTiles = size * size
        var newTiles: List<Int>
        do {
            newTiles = (0 until totalTiles).toList().shuffled()
        } while (!isSolvable(newTiles, size))

        _uiState.update {
            it.copy(
                gridSize = size,
                tiles = newTiles,
                moves = 0,
                timeElapsedSec = 0,
                isSolved = false,
                isGameActive = true
            )
        }
        startTimer()
    }

    fun restartCurrentGame() {
        timerJob?.cancel()
        viewModelScope.launch { repository.clearState() }
        val size = _uiState.value.gridSize
        if (size < 2) return

        val totalTiles = size * size
        var newTiles: List<Int>
        do {
            newTiles = (0 until totalTiles).toList().shuffled()
        } while (!isSolvable(newTiles, size))

        _uiState.update {
            it.copy(
                tiles = newTiles,
                moves = 0,
                timeElapsedSec = 0,
                isSolved = false,
                isGameActive = true
            )
        }
        startTimer()
    }

    fun onTileClicked(index: Int) {
        val state = _uiState.value
        if (state.isSolved || !state.isGameActive) return

        val tiles = state.tiles.toMutableList()
        val emptyIndex = tiles.indexOf(0)
        val size = state.gridSize

        if (isNeighbor(index, emptyIndex, size)) {
            tiles[emptyIndex] = tiles[index]
            tiles[index] = 0

            val newMoves = state.moves + 1
            val isNowSolved = checkWinCondition(tiles, size)
            if (isNowSolved) {
                timerJob?.cancel()
                viewModelScope.launch { repository.clearState() }
            }

            _uiState.update {
                it.copy(
                    tiles = tiles,
                    moves = newMoves,
                    isSolved = isNowSolved
                )
            }

            if (!isNowSolved) {
                saveCurrentState()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isGameActive && !_uiState.value.isSolved) {
                    _uiState.update { it.copy(timeElapsedSec = it.timeElapsedSec + 1) }
                    if (_uiState.value.timeElapsedSec % 5 == 0) {
                        saveCurrentState()
                    }
                }
            }
        }
    }

    private fun saveCurrentState() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isGameActive && !state.isSolved) {
                repository.saveState(
                    PuzzleStateRepository.PuzzleState(
                        tiles = state.tiles,
                        gridSize = state.gridSize,
                        moves = state.moves,
                        timeElapsedSec = state.timeElapsedSec
                    )
                )
            }
        }
    }

    // --- Helper Functions ---
    private fun isNeighbor(index1: Int, index2: Int, size: Int): Boolean {
        val row1 = index1 / size; val col1 = index1 % size
        val row2 = index2 / size; val col2 = index2 % size
        return (row1 == row2 && abs(col1 - col2) == 1) || (col1 == col2 && abs(row1 - row2) == 1)
    }

    private fun checkWinCondition(currentTiles: List<Int>, size: Int): Boolean {
        val totalTiles = size * size
        val solvedState = (1 until totalTiles).toList() + 0
        return currentTiles == solvedState
    }

    private fun isSolvable(tiles: List<Int>, size: Int): Boolean {
        val totalTiles = size * size; var inversions = 0
        val flatTiles = tiles.filter { it != 0 }
        for (i in 0 until totalTiles - 1) {
            for (j in i + 1 until totalTiles - 1) {
                if (flatTiles[i] > flatTiles[j]) { inversions++ }
            }
        }
        return if (size % 2 == 1) { inversions % 2 == 0 }
        else {
            val emptyRowFromBottom = size - (tiles.indexOf(0) / size)
            if (emptyRowFromBottom % 2 == 0) { inversions % 2 == 1 }
            else { inversions % 2 == 0 }
        }
    }
}

// The UiState data class is now simpler without the "isSolving" state
data class SlidingPuzzleUiState(
    val selectedGridSize: Int = 0,
    val gridSize: Int = 3,
    val tiles: List<Int> = emptyList(),
    val moves: Int = 0,
    val timeElapsedSec: Int = 0,
    val isSolved: Boolean = false,
    val isGameActive: Boolean = false
)