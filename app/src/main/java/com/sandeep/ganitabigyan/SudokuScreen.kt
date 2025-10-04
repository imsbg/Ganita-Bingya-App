// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.ui.theme.SudokuSelectedCell
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
                Button(onClick = { sudokuViewModel.showDifficultySelector.value = true }) {
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
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
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
                    CircularProgressIndicator()
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val buttonShape = RoundedCornerShape(16.dp)
                Button(onClick = { onDifficultySelected(Difficulty.EASY) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = buttonShape) {
                    Text(stringResource(id = R.string.difficulty_easy), fontSize = 16.sp)
                }
                Button(onClick = { onDifficultySelected(Difficulty.MEDIUM) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = buttonShape) {
                    Text(stringResource(id = R.string.difficulty_medium), fontSize = 16.sp)
                }
                Button(onClick = { onDifficultySelected(Difficulty.HARD) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = buttonShape) {
                    Text(stringResource(id = R.string.difficulty_hard), fontSize = 16.sp)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val buttonSize = 56.dp
        // <<< FIX: Replaced `elevation` with `shadowElevation` for Material 3 >>>
        // Undo Button
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, shadowElevation = 4.dp) {
            IconButton(onClick = onUndoClick, enabled = canUndo, modifier = Modifier.size(buttonSize)) {
                Icon(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.undo), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        // Hint Button
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer, shadowElevation = 4.dp) {
            IconButton(onClick = onHintClick, modifier = Modifier.size(buttonSize)) {
                Icon(Icons.Outlined.Lightbulb, stringResource(R.string.hint), tint = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        }
        // Redo Button
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, shadowElevation = 4.dp) {
            IconButton(onClick = onRedoClick, enabled = canRedo, modifier = Modifier.size(buttonSize)) {
                Icon(Icons.AutoMirrored.Filled.Redo, stringResource(R.string.redo), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
fun SudokuBoard(
    userBoard: Array<IntArray>, puzzleBoard: Array<IntArray>, selectedCell: CellPosition?,
    incorrectCells: Set<CellPosition>, boardSize: Int, onCellClick: (row: Int, col: Int) -> Unit
) {
    val isEasyMode = boardSize == 4
    val subGridSize = if (isEasyMode) 2 else 3
    val boardPadding = 16.dp

    Card(
        modifier = Modifier
            .padding(horizontal = boardPadding)
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (i in 0 until boardSize) {
                    if (i > 0 && i % subGridSize == 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Row(
                        Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        for (j in 0 until boardSize) {
                            if (j > 0 && j % subGridSize == 0) {
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            SudokuCell(
                                number = userBoard[i][j], isOriginal = puzzleBoard[i][j] != 0,
                                isSelected = selectedCell?.row == i && selectedCell?.col == j,
                                isIncorrect = incorrectCells.contains(CellPosition(i, j)),
                                boardSize = boardSize,
                                onClick = { onCellClick(i, j) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SudokuCell(
    number: Int, isOriginal: Boolean, isSelected: Boolean, isIncorrect: Boolean,
    boardSize: Int, onClick: () -> Unit
) {
    val context = LocalContext.current
    val isEasyMode = boardSize == 4

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> SudokuSelectedCell
            isIncorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.surface
        }, label = "cell background color"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isIncorrect -> MaterialTheme.colorScheme.onErrorContainer
            isOriginal -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.primary
        }, label = "cell text color"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (number != 0) {
            Text(
                text = number.toLocaleNumerals(context),
                fontSize = if (isEasyMode) 32.sp else 24.sp,
                fontWeight = if (isOriginal) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

// <<< FIX: Added the required @OptIn annotation for FlowRow >>>
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberPad(boardSize: Int, onNumberClick: (Int) -> Unit, onEraseClick: () -> Unit) {
    val context = LocalContext.current
    val numberRange = 1..boardSize
    val buttonSize = if (boardSize == 4) 60.dp else 52.dp

    FlowRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        numberRange.forEach { number ->
            NumberButton(
                text = number.toLocaleNumerals(context),
                onClick = { onNumberClick(number) },
                size = buttonSize
            )
        }
        FilledTonalButton(
            onClick = onEraseClick,
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Outlined.Backspace, contentDescription = stringResource(id = R.string.erase))
        }
    }
}

@Composable
fun NumberButton(text: String, onClick: () -> Unit, size: Dp) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}