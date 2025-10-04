// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Collections
import java.util.Stack

data class CellPosition(val row: Int, val col: Int)
private data class Move(val cell: CellPosition, val previousValue: Int, val newValue: Int)
enum class Difficulty { EASY, MEDIUM, HARD }

class SudokuViewModel : ViewModel() {

    val boardSize = mutableStateOf(9)
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
                generateRandomPuzzle4x4()
            }
            Difficulty.MEDIUM -> {
                boardSize.value = 9
                generateRandomPuzzle9x9(holesToMake = 45)
            }
            Difficulty.HARD -> {
                boardSize.value = 9
                generateRandomPuzzle9x9(holesToMake = 55)
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
        // When getting a hint, we need to manually update the selected cell
        // before entering the number, to ensure the UI focuses on the hint cell.
        selectCell(selected.row, selected.col)
        val correctNumber = solution[selected.row][selected.col]
        enterNumber(correctNumber)
    }
    private fun findFirstEmptyCell(): CellPosition? {
        for (r in 0 until boardSize.value) {
            for (c in 0 until boardSize.value) {
                if (_userBoard.value[r][c] == 0) {
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

    // <<< FUNCTION REPLACED WITH CORRECT LOGIC >>>
    private fun validateBoard() {
        val currentBoard = _userBoard.value
        val errors = mutableSetOf<CellPosition>()
        // Iterate through every cell on the board
        for (i in 0 until boardSize.value) {
            for (j in 0 until boardSize.value) {
                val userNumber = currentBoard[i][j]
                // We only care about cells the user has filled in
                if (userNumber != 0 && _puzzleBoard.value[i][j] == 0) {
                    // If the user's number doesn't match the solution, it's an error.
                    if (userNumber != solution[i][j]) {
                        errors.add(CellPosition(i, j))
                    }
                }
            }
        }
        _incorrectCells.value = errors
    }

    // <<< The old isValidPlacement function is no longer needed and has been removed >>>

    private fun checkIfSolved() {
        val isFull = _userBoard.value.all { row -> row.all { it != 0 } }
        // The puzzle is solved if the board is full AND there are no incorrect cells.
        if (isFull && _incorrectCells.value.isEmpty()) {
            isSolved.value = true
        }
    }

    // --- NEW RANDOM PUZZLE GENERATION LOGIC (UNCHANGED) ---

    private fun generateRandomPuzzle9x9(holesToMake: Int): Pair<Array<IntArray>, Array<IntArray>> {
        var solvedBoard = arrayOf(
            intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9), intArrayOf(4, 5, 6, 7, 8, 9, 1, 2, 3),
            intArrayOf(7, 8, 9, 1, 2, 3, 4, 5, 6), intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 1),
            intArrayOf(5, 6, 7, 8, 9, 1, 2, 3, 4), intArrayOf(8, 9, 1, 2, 3, 4, 5, 6, 7),
            intArrayOf(3, 4, 5, 6, 7, 8, 9, 1, 2), intArrayOf(6, 7, 8, 9, 1, 2, 3, 4, 5),
            intArrayOf(9, 1, 2, 3, 4, 5, 6, 7, 8)
        )
        repeat(50) {
            val num1 = (1..9).random(); var num2: Int
            do { num2 = (1..9).random() } while (num1 == num2)
            solvedBoard = swapNumbers(solvedBoard, num1, num2)
            val block = (0..2).random()
            val row1 = (0..2).random(); var row2: Int
            do { row2 = (0..2).random() } while (row1 == row2)
            solvedBoard = swapRows(solvedBoard, block * 3 + row1, block * 3 + row2)
            val col1 = (0..2).random(); var col2: Int
            do { col2 = (0..2).random() } while (col1 == col2)
            solvedBoard = swapCols(solvedBoard, block * 3 + col1, block * 3 + col2)
        }
        val puzzle = solvedBoard.map { it.clone() }.toTypedArray()
        var holes = holesToMake
        while (holes > 0) {
            val r = (0..8).random(); val c = (0..8).random()
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0
                holes--
            }
        }
        return Pair(solvedBoard, puzzle)
    }

    private fun generateRandomPuzzle4x4(): Pair<Array<IntArray>, Array<IntArray>> {
        var solvedBoard = arrayOf(
            intArrayOf(1, 2, 3, 4), intArrayOf(3, 4, 1, 2),
            intArrayOf(2, 1, 4, 3), intArrayOf(4, 3, 2, 1)
        )
        repeat(10) {
            val num1 = (1..4).random(); var num2: Int
            do { num2 = (1..4).random() } while (num1 == num2)
            solvedBoard = swapNumbers(solvedBoard, num1, num2)
            val row1 = (0..3).random(); var row2: Int
            do { row2 = (0..3).random() } while (row1 == row2)
            solvedBoard = swapRows(solvedBoard, row1, row2)
        }
        val puzzle = solvedBoard.map { it.clone() }.toTypedArray()
        var holes = 8
        while (holes > 0) {
            val r = (0..3).random(); val c = (0..3).random()
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0
                holes--
            }
        }
        return Pair(solvedBoard, puzzle)
    }
    private fun swapNumbers(board: Array<IntArray>, n1: Int, n2: Int): Array<IntArray> {
        val newBoard = board.map { it.clone() }.toTypedArray()
        for (i in newBoard.indices) {
            for (j in newBoard[i].indices) {
                if (newBoard[i][j] == n1) newBoard[i][j] = n2
                else if (newBoard[i][j] == n2) newBoard[i][j] = n1
            }
        }
        return newBoard
    }
    private fun swapRows(board: Array<IntArray>, r1: Int, r2: Int): Array<IntArray> {
        val newBoard = board.map { it.clone() }.toTypedArray()
        val temp = newBoard[r1]
        newBoard[r1] = newBoard[r2]
        newBoard[r2] = temp
        return newBoard
    }
    private fun swapCols(board: Array<IntArray>, c1: Int, c2: Int): Array<IntArray> {
        val newBoard = board.map { it.clone() }.toTypedArray()
        for (i in newBoard.indices) {
            val temp = newBoard[i][c1]
            newBoard[i][c1] = newBoard[i][c2]
            newBoard[i][c2] = temp
        }
        return newBoard
    }
}