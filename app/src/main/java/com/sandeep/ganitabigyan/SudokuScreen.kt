// FILE: app/src/main/java/com/sandeep/ganitabigyan/SudokuScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SudokuScreen(
    navController: NavController,
    sudokuViewModel: SudokuViewModel = viewModel()
) {
    val userBoard by sudokuViewModel.userBoard.collectAsState()
    val puzzleBoard by sudokuViewModel.puzzleBoard.collectAsState()
    val selectedCell by sudokuViewModel.selectedCell
    val incorrectCells by sudokuViewModel.incorrectCells.collectAsState()
    val isSolved by sudokuViewModel.isSolved
    val showDifficultySelector by sudokuViewModel.showDifficultySelector
    val boardSize by sudokuViewModel.boardSize

    if (showDifficultySelector) {
        DifficultySelectorDialog(
            onDifficultySelected = { sudokuViewModel.generateNewPuzzle(it) },
            onDismiss = { if (puzzleBoard.isEmpty()) navController.popBackStack() }
        )
    }

    if (isSolved) {
        AlertDialog(
            onDismissRequest = { sudokuViewModel.showDifficultySelector.value = true },
            title = { Text(stringResource(id = R.string.congratulations)) },
            text = { Text(stringResource(id = R.string.puzzle_solved)) },
            confirmButton = {
                TextButton(onClick = { sudokuViewModel.showDifficultySelector.value = true }) {
                    Text(stringResource(id = R.string.play_again))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.menu_sudoku)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                },
                actions = {
                    IconButton(onClick = { sudokuViewModel.showDifficultySelector.value = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(id = R.string.new_game))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            if (userBoard.isNotEmpty()) {
                SudokuBoard(
                    userBoard = userBoard,
                    puzzleBoard = puzzleBoard,
                    selectedCell = selectedCell,
                    incorrectCells = incorrectCells,
                    boardSize = boardSize,
                    onCellClick = { row, col -> sudokuViewModel.selectCell(row, col) }
                )

                ActionButtons(
                    onUndoClick = { sudokuViewModel.undo() },
                    onRedoClick = { sudokuViewModel.redo() },
                    onHintClick = { sudokuViewModel.getHint() },
                    canUndo = sudokuViewModel.canUndo.value,
                    canRedo = sudokuViewModel.canRedo.value
                )

                NumberPad(
                    boardSize = boardSize,
                    onNumberClick = { sudokuViewModel.enterNumber(it) },
                    onEraseClick = { sudokuViewModel.eraseNumber() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.select_difficulty_prompt))
                }
            }
        }
    }
}

@Composable
fun DifficultySelectorDialog(
    onDifficultySelected: (Difficulty) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.select_difficulty)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDifficultySelected(Difficulty.EASY) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.difficulty_easy))
                }
                Button(onClick = { onDifficultySelected(Difficulty.MEDIUM) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.difficulty_medium))
                }
                Button(onClick = { onDifficultySelected(Difficulty.HARD) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.difficulty_hard))
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ActionButtons(
    onUndoClick: () -> Unit, onRedoClick: () -> Unit, onHintClick: () -> Unit,
    canUndo: Boolean, canRedo: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onUndoClick, enabled = canUndo) { Icon(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.undo)) }
        IconButton(onClick = onRedoClick, enabled = canRedo) { Icon(Icons.AutoMirrored.Filled.Redo, stringResource(R.string.redo)) }
        IconButton(onClick = onHintClick) { Icon(Icons.Outlined.Lightbulb, stringResource(R.string.hint)) }
    }
}

@Composable
fun SudokuBoard(
    userBoard: Array<IntArray>, puzzleBoard: Array<IntArray>, selectedCell: CellPosition?,
    incorrectCells: Set<CellPosition>, boardSize: Int, onCellClick: (row: Int, col: Int) -> Unit
) {
    val isEasyMode = boardSize == 4
    val boardPadding = if (isEasyMode) 32.dp else 16.dp
    val outerBorder = if (isEasyMode) 4.dp else 2.dp

    Box(
        modifier = Modifier
            .padding(horizontal = boardPadding)
            .aspectRatio(1f)
            .border(outerBorder, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(if (isEasyMode) 12.dp else 0.dp))
            .clip(RoundedCornerShape(if (isEasyMode) 12.dp - outerBorder else 0.dp))
    ) {
        Column(Modifier.fillMaxSize()) {
            for (i in 0 until boardSize) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    for (j in 0 until boardSize) {
                        SudokuCell(
                            number = userBoard[i][j], isOriginal = puzzleBoard[i][j] != 0,
                            isSelected = selectedCell?.row == i && selectedCell?.col == j,
                            isIncorrect = incorrectCells.contains(CellPosition(i, j)),
                            rowIndex = i, colIndex = j, boardSize = boardSize,
                            onClick = { onCellClick(i, j) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SudokuCell(
    number: Int, isOriginal: Boolean, isSelected: Boolean, isIncorrect: Boolean,
    rowIndex: Int, colIndex: Int, boardSize: Int, onClick: () -> Unit
) {
    val context = LocalContext.current
    val isEasyMode = boardSize == 4
    val subGridSize = if (isEasyMode) 2 else 3

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isIncorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        isOriginal -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        isIncorrect -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.primary
    }

    val bottomBorder = if (rowIndex % subGridSize == subGridSize - 1 && rowIndex != boardSize - 1) (if (isEasyMode) 3.dp else 2.dp) else 0.5.dp
    val endBorder = if (colIndex % subGridSize == subGridSize - 1 && colIndex != boardSize - 1) (if (isEasyMode) 3.dp else 2.dp) else 0.5.dp
    val cellShape = if (isEasyMode) RoundedCornerShape(8.dp) else RoundedCornerShape(0.dp)

    Box(
        modifier = Modifier
            .weight(1f).aspectRatio(1f)
            .background(backgroundColor, if (isEasyMode) cellShape else MaterialTheme.shapes.extraSmall)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = if (isEasyMode) 0.2f else 0.5f))
            .border(width = bottomBorder, color = MaterialTheme.colorScheme.onSurface)
            .border(width = endBorder, color = MaterialTheme.colorScheme.onSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (number != 0) {
            Text(
                text = number.toLocaleNumerals(context),
                fontSize = if (isEasyMode) 28.sp else 20.sp,
                fontWeight = if (isOriginal) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
fun NumberPad(boardSize: Int, onNumberClick: (Int) -> Unit, onEraseClick: () -> Unit) {
    val context = LocalContext.current
    val isEasyMode = boardSize == 4
    val numberRange = if (isEasyMode) 1..4 else 1..9
    val buttonSize = if (isEasyMode) 64.dp else 56.dp
    val buttonShape = RoundedCornerShape(if (isEasyMode) 16.dp else 12.dp)

    Column(
        modifier = Modifier.padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isEasyMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                (1..4).forEach { NumberButton(it.toLocaleNumerals(context), { onNumberClick(it) }, buttonSize, buttonShape) }
                OutlinedButton(onClick = onEraseClick, modifier = Modifier.size(buttonSize).clip(buttonShape), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Outlined.Backspace, contentDescription = stringResource(id = R.string.erase))
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { NumberButton(it.toLocaleNumerals(context), { onNumberClick(it) }, buttonSize, buttonShape) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (6..9).forEach { NumberButton(it.toLocaleNumerals(context), { onNumberClick(it) }, buttonSize, buttonShape) }
                OutlinedButton(onClick = onEraseClick, modifier = Modifier.size(buttonSize).clip(buttonShape), contentPadding = PaddingValues(0.dp)) {
                    Icon(Icons.Outlined.Backspace, contentDescription = stringResource(id = R.string.erase))
                }
            }
        }
    }
}

@Composable
fun NumberButton(text: String, onClick: () -> Unit, size: Dp, shape: RoundedCornerShape) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size).clip(shape),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 24.sp)
    }
}