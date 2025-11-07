// FILE: app/src/main/java/com/sandeep/ganitabigyan/SlidingBlockPuzzleScreen.kt
package com.sandeep.ganitabigyan

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlidingBlockPuzzleScreen(
    navController: NavController,
    viewModel: SlidingBlockPuzzleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.sliding_block_puzzle_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
        ) {
            if (uiState.selectedGridSize == 0) {
                GridSizeSelector(viewModel = viewModel)
            } else {
                GameBoard(uiState = uiState, viewModel = viewModel)
            }

            AnimatedVisibility(
                visible = uiState.isSolved,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                WinOverlay(uiState = uiState, onNextLevel = {
                    val nextSize = uiState.gridSize + 1
                    viewModel.selectGridSize(nextSize)
                })
            }
        }
    }
}

@Composable
fun GridSizeSelector(viewModel: SlidingBlockPuzzleViewModel) {
    val hasSavedGame by viewModel.hasSavedGame.collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = hasSavedGame) {
            Button(
                onClick = { viewModel.continueLastGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                AutoResizeText(text = stringResource(id = R.string.continue_last_game), style = MaterialTheme.typography.bodyLarge)
            }
        }

        Text(
            text = stringResource(id = R.string.choose_grid_size),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(8) { index ->
                val size = index + 2
                Button(
                    onClick = { viewModel.selectGridSize(size) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Text(text = "$size x $size", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameBoard(uiState: SlidingPuzzleUiState, viewModel: SlidingBlockPuzzleViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoCard(title = stringResource(R.string.moves), value = uiState.moves.toString())
            InfoCard(title = stringResource(R.string.time), value = formatTime(uiState.timeElapsedSec))
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(uiState.gridSize),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(uiState.tiles) { index, tileValue ->
                PuzzleTile(
                    number = tileValue,
                    gridSize = uiState.gridSize,
                    onClick = { viewModel.onTileClicked(index) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // <<< AUTO-SOLVE BUTTON REMOVED FROM HERE >>>
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.restartCurrentGame() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                AutoResizeText(text = stringResource(id = R.string.restart))
            }

            OutlinedButton(onClick = { viewModel.selectGridSize(0) }) {
                AutoResizeText(text = stringResource(id = R.string.change_grid_size))
            }
        }
    }
}

// All other Composable functions (PuzzleTile, InfoCard, WinOverlay, etc.) remain unchanged
@Composable
fun PuzzleTile(number: Int, gridSize: Int, onClick: () -> Unit) {
    val context = LocalContext.current
    val isVisible = number != 0
    val scale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.8f, label = "tileScale")

    AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut()) {
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = number.toLocaleNumerals(context),
                    fontSize = when {
                        gridSize <= 4 -> 32.sp
                        gridSize <= 6 -> 24.sp
                        else -> 18.sp
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value.toLocaleNumerals(context),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun WinOverlay(uiState: SlidingPuzzleUiState, onNextLevel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false, onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth(0.8f)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AutoResizeText(
                    text = stringResource(id = R.string.level_complete),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Moves")
                        Text("${uiState.moves}", fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally){
                        Icon(Icons.Default.Timer, contentDescription = "Time")
                        Text(formatTime(uiState.timeElapsedSec), fontWeight = FontWeight.SemiBold)
                    }
                }

                if (uiState.gridSize < 9) {
                    Button(onClick = onNextLevel, modifier = Modifier.fillMaxWidth()) {
                        AutoResizeText(text = stringResource(id = R.string.next_level))
                    }
                } else {
                    AutoResizeText(text = stringResource(id = R.string.congratulations_puzzle_solved))
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}