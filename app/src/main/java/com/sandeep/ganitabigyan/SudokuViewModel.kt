// FILE: app/src/main/java/com/sandeep/ganitabigyan/SudokuViewModel.kt

package com.sandeep.ganitabigyan

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Stack

data class CellPosition(val row: Int, val col: Int)
private data class Move(val cell: CellPosition, val previousValue: Int, val newValue: Int)
enum class Difficulty { EASY, MEDIUM, HARD }

class SudokuViewModel : ViewModel() {

    // --- STATE MANAGEMENT ---
    val boardSize = mutableStateOf(9) // NEW: Can be 4 or 9

    private val _puzzleBoard = MutableStateFlow<Array<IntArray>>(emptyArray())
    val puzzleBoard: StateFlow<Array<IntArray>> = _puzzleBoard

    private val _userBoard = MutableStateFlow<Array<IntArray>>(emptyArray())
    val userBoard: StateFlow<Array<IntArray>> = _userBoard

    val selectedCell = mutableStateOf<CellPosition?>(null)
    private val _incorrectCells = MutableStateFlow<Set<CellPosition>>(emptySet())
    val incorrectCells: StateFlow<Set<CellPosition>> = _incorrectCells
    val isSolved = mutableStateOf(false)
    val showDifficultySelector = mutableStateOf(true)
    private var solution: Array<IntArray> = emptyArray()

    private val undoStack = Stack<Move>()
    private val redoStack = Stack<Move>()
    val canUndo = mutableStateOf(false)
    val canRedo = mutableStateOf(false)

    fun generateNewPuzzle(difficulty: Difficulty) {
        val puzzleData = when (difficulty) {
            Difficulty.EASY -> {
                boardSize.value = 4
                getPredefinedPuzzle4x4()
            }
            Difficulty.MEDIUM -> {
                boardSize.value = 9
                getPredefinedPuzzle9x9(holesToMake = 45)
            }
            Difficulty.HARD -> {
                boardSize.value = 9
                getPredefinedPuzzle9x9(holesToMake = 55)
            }
        }

        solution = puzzleData.first
        _puzzleBoard.value = puzzleData.second
        _userBoard.value = puzzleData.second.map { it.clone() }.toTypedArray()

        resetGameState()
        showDifficultySelector.value = false
    }

    fun selectCell(row: Int, col: Int) {
        if (_puzzleBoard.value.getOrNull(row)?.getOrNull(col) == 0) {
            selectedCell.value = CellPosition(row, col)
        }
    }

    fun enterNumber(number: Int) {
        val selected = selectedCell.value ?: return
        val previousValue = _userBoard.value[selected.row][selected.col]
        if (previousValue == number) return

        undoStack.push(Move(selected, previousValue, number))
        redoStack.clear()
        updateHistoryButtons()

        updateBoardValue(selected, number)
        validateBoard()
        checkIfSolved()
    }

    fun eraseNumber() { enterNumber(0) }
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastMove = undoStack.pop()
            redoStack.push(lastMove)
            updateHistoryButtons()
            updateBoardValue(lastMove.cell, lastMove.previousValue, isUndoRedo = true)
            validateBoard()
        }
    }
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val nextMove = redoStack.pop()
            undoStack.push(nextMove)
            updateHistoryButtons()
            updateBoardValue(nextMove.cell, nextMove.newValue, isUndoRedo = true)
            validateBoard()
        }
    }
    fun getHint() {
        val selected = selectedCell.value ?: findFirstEmptyCell() ?: return
        val correctNumber = solution[selected.row][selected.col]
        enterNumber(correctNumber)
    }

    private fun findFirstEmptyCell(): CellPosition? {
        for (r in 0 until boardSize.value) {
            for (c in 0 until boardSize.value) {
                if (_userBoard.value[r][c] == 0) {
                    selectCell(r, c)
                    return CellPosition(r,c)
                }
            }
        }
        return null
    }
    private fun updateBoardValue(cell: CellPosition, value: Int, isUndoRedo: Boolean = false) {
        val newBoard = _userBoard.value.map { it.clone() }.toTypedArray()
        newBoard[cell.row][cell.col] = value
        _userBoard.value = newBoard
        if (!isUndoRedo) selectedCell.value = cell
    }
    private fun resetGameState() {
        selectedCell.value = null
        _incorrectCells.value = emptySet()
        isSolved.value = false
        undoStack.clear()
        redoStack.clear()
        updateHistoryButtons()
    }
    private fun updateHistoryButtons() {
        canUndo.value = undoStack.isNotEmpty()
        canRedo.value = redoStack.isNotEmpty()
    }

    private fun validateBoard() {
        val currentBoard = _userBoard.value
        val errors = mutableSetOf<CellPosition>()
        for (i in 0 until boardSize.value) {
            for (j in 0 until boardSize.value) {
                val num = currentBoard[i][j]
                if (num == 0 || _puzzleBoard.value[i][j] != 0) continue
                if (!isValidPlacement(currentBoard, num, i, j)) {
                    errors.add(CellPosition(i, j))
                }
            }
        }
        _incorrectCells.value = errors
    }

    private fun isValidPlacement(board: Array<IntArray>, number: Int, row: Int, col: Int): Boolean {
        val size = boardSize.value
        val subGridSize = if (size == 9) 3 else 2

        for (i in 0 until size) {
            if (i != col && board[row][i] == number) return false
            if (i != row && board[i][col] == number) return false
        }
        val startRow = row - row % subGridSize
        val startCol = col - col % subGridSize
        for (i in 0 until subGridSize) {
            for (j in 0 until subGridSize) {
                if ((startRow + i != row || startCol + j != col) && board[startRow + i][startCol + j] == number) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkIfSolved() {
        val isFull = _userBoard.value.all { row -> row.all { it != 0 } }
        if (isFull && _incorrectCells.value.isEmpty()) { isSolved.value = true }
    }

    private fun getPredefinedPuzzle9x9(holesToMake: Int): Pair<Array<IntArray>, Array<IntArray>> {
        val solvedBoard = arrayOf(
            intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2), intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8), intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
            intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3), intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1), intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
            intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4), intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5), intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
        )
        val puzzle = solvedBoard.map { it.clone() }.toTypedArray()
        var holes = holesToMake
        val random = java.util.Random()
        while (holes > 0) {
            val r = random.nextInt(9); val c = random.nextInt(9)
            if (puzzle[r][c] != 0) { puzzle[r][c] = 0; holes-- }
        }
        return Pair(solvedBoard, puzzle)
    }

    private fun getPredefinedPuzzle4x4(): Pair<Array<IntArray>, Array<IntArray>> {
        val solvedBoard = arrayOf(
            intArrayOf(4, 3, 1, 2),
            intArrayOf(1, 2, 4, 3),
            intArrayOf(2, 1, 3, 4),
            intArrayOf(3, 4, 2, 1)
        )
        val puzzleBoard = arrayOf(
            intArrayOf(0, 3, 1, 0),
            intArrayOf(1, 0, 0, 3),
            intArrayOf(2, 0, 0, 4),
            intArrayOf(0, 4, 2, 0)
        )
        return Pair(solvedBoard, puzzleBoard)
    }
}