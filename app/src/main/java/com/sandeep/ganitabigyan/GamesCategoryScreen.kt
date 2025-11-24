// FILE: app/src/main/java/com/sandeep/ganitabigyan/GamesCategoryScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections

private data class GameItem(
    val id: String, // Use route as a unique ID
    val titleResId: Int,
    val subtitleResId: Int,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GamesCategoryScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gameOrderRepository = remember { GameOrderRepository(context) }

    val allGames = remember {
        listOf(
            GameItem(AppDestinations.GAME_ROUTE, R.string.menu_start_game, R.string.menu_start_game_desc, AppDestinations.GAME_ROUTE, listOf(Color(0xFFFFB74D), Color(0xFFFF9800)), Icons.Default.PlayArrow),
            GameItem(AppDestinations.LOGIC_GAME_ROUTE, R.string.menu_logic_game, R.string.menu_logic_game_desc, AppDestinations.LOGIC_GAME_ROUTE, listOf(Color(0xFF7986CB), Color(0xFF3F51B5)), Icons.Default.Psychology),
            GameItem(AppDestinations.WORD_PROBLEM_GAME_ROUTE, R.string.word_problem_game_title, R.string.word_problem_game_desc, AppDestinations.WORD_PROBLEM_GAME_ROUTE, listOf(Color(0xFFE57373), Color(0xFFD32F2F)), Icons.Default.Calculate),
            GameItem(AppDestinations.SUDOKU_ROUTE, R.string.menu_sudoku, R.string.menu_sudoku_desc, AppDestinations.SUDOKU_ROUTE, listOf(Color(0xFFBA68C8), Color(0xFF9C27B0)), Icons.Default.ViewModule),
            GameItem(AppDestinations.VISUAL_GAME_ROUTE, R.string.menu_visual_game, R.string.menu_visual_game_desc, AppDestinations.VISUAL_GAME_ROUTE, listOf(Color(0xFF81C784), Color(0xFF4CAF50)), Icons.Default.Compare),
            GameItem(AppDestinations.FTMN_GAME_ROUTE, R.string.menu_ftmn_game, R.string.menu_ftmn_game_desc, AppDestinations.FTMN_GAME_ROUTE, listOf(Color(0xFF4DB6AC), Color(0xFF009688)), Icons.Default.HelpOutline),
            GameItem(AppDestinations.SLIDING_BLOCK_PUZZLE_ROUTE, R.string.sliding_block_puzzle_title, R.string.sliding_block_puzzle_desc, AppDestinations.SLIDING_BLOCK_PUZZLE_ROUTE, listOf(Color(0xFF4DD0E1), Color(0xFF00ACC1)), Icons.Default.Dashboard)
        )
    }

    var orderedGames by remember { mutableStateOf<List<GameItem>>(emptyList()) }
    var isReorderMode by remember { mutableStateOf(false) }
    var isOrderChanged by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedOrder = gameOrderRepository.gameOrderFlow.first()
        orderedGames = if (savedOrder.isEmpty()) {
            allGames
        } else {
            val gamesMap = allGames.associateBy { it.id }
            val loadedGames = savedOrder.mapNotNull { gamesMap[it] }
            isOrderChanged = loadedGames.map { it.id } != allGames.map { it.id }
            loadedGames
        }
    }

    val lazyListState = rememberLazyListState()

    // --- Function to handle moving an item up ---
    val onMoveUp = { index: Int ->
        if (index > 0) {
            val newList = orderedGames.toMutableList().apply {
                Collections.swap(this, index, index - 1)
            }
            orderedGames = newList
            isOrderChanged = true
        }
    }

    // --- Function to handle moving an item down ---
    val onMoveDown = { index: Int ->
        if (index < orderedGames.size - 1) {
            val newList = orderedGames.toMutableList().apply {
                Collections.swap(this, index, index + 1)
            }
            orderedGames = newList
            isOrderChanged = true
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.games_category_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                },
                actions = {
                    AnimatedVisibility(visible = isReorderMode && isOrderChanged) {
                        IconButton(onClick = {
                            scope.launch { gameOrderRepository.clearOrder() }
                            orderedGames = allGames
                            isOrderChanged = false
                        }) {
                            Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.reset_order_description))
                        }
                    }

                    IconButton(onClick = {
                        isReorderMode = !isReorderMode
                        if (!isReorderMode && isOrderChanged) {
                            scope.launch { gameOrderRepository.saveOrder(orderedGames.map { it.id }) }
                        }
                    }) {
                        Icon(
                            imageVector = if (isReorderMode) Icons.Default.Done else Icons.Default.Sort,
                            contentDescription = stringResource(if (isReorderMode) R.string.save_order_description else R.string.reorder_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(orderedGames, key = { _, item -> item.id }) { index, item ->
                Box(modifier = Modifier
                    .animateItemPlacement(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ){
                    GameItemCard(
                        item = item,
                        isReorderMode = isReorderMode,
                        onClick = { if (!isReorderMode) navController.navigate(item.route) },
                        // Pass index and callbacks for the buttons
                        index = index,
                        totalItemCount = orderedGames.size,
                        onMoveUp = { onMoveUp(index) },
                        onMoveDown = { onMoveDown(index) }
                    )
                }
            }
        }
    }
}


@Composable
private fun GameItemCard(
    item: GameItem,
    isReorderMode: Boolean,
    onClick: () -> Unit,
    // New parameters for button controls
    index: Int,
    totalItemCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick, enabled = !isReorderMode),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.background(brush = Brush.horizontalGradient(colors = item.gradient))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = isReorderMode) {
                    // --- Up/Down Button Controls ---
                    Column(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = onMoveUp, enabled = index > 0) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(R.string.move_up_description),
                                tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(onClick = onMoveDown, enabled = index < totalItemCount - 1) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(R.string.move_down_description),
                                tint = if (index < totalItemCount - 1) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    AutoResizeText(
                        text = stringResource(id = item.titleResId),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                    AutoResizeText(
                        text = stringResource(id = item.subtitleResId),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    var scaledTextStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                scaledTextStyle = scaledTextStyle.copy(
                    fontSize = scaledTextStyle.fontSize * 0.95
                )
            } else {
                readyToDraw = true
            }
        },
        style = scaledTextStyle,
        softWrap = false,
        maxLines = 1
    )
}