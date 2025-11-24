// FILE: app/src/main/java/com/sandeep/ganitabigyan/LearningCategoryScreen.kt

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Collections

private data class LearningItem(
    val id: String,
    val titleResId: Int,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LearningCategoryScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val learningOrderRepository = remember { LearningOrderRepository(context) }

    val allLearningItems = remember {
        listOf(
            LearningItem(AppDestinations.NUMBERS_ROUTE, R.string.menu_numbers, AppDestinations.NUMBERS_ROUTE, listOf(Color(0xFFE57373), Color(0xFFF44336)), Icons.Default.LooksOne),
            LearningItem(AppDestinations.ORDINALS_ROUTE, R.string.menu_ordinals, AppDestinations.ORDINALS_ROUTE, listOf(Color(0xFF64B5F6), Color(0xFF2196F3)), Icons.Default.FormatListNumbered),
            LearningItem(AppDestinations.GANANA_ROUTE, R.string.menu_ganana, AppDestinations.GANANA_ROUTE, listOf(Color(0xFF26A69A), Color(0xFF00897B)), Icons.Default.Functions),
            LearningItem(AppDestinations.PANIKIA_LIST_ROUTE, R.string.menu_panikia, AppDestinations.PANIKIA_LIST_ROUTE, listOf(Color(0xFFBA68C8), Color(0xFF9C27B0)), Icons.Default.MenuBook),
            LearningItem(id = AppDestinations.ROMAN_ROUTE, titleResId = R.string.menu_roman_numbers, route = AppDestinations.ROMAN_ROUTE, gradient = listOf(Color(0xFFFFB74D), Color(0xFFF57C00)), Icons.Default.Close),
            LearningItem(AppDestinations.DRAWING_ROUTE, R.string.menu_drawing_pad, AppDestinations.DRAWING_ROUTE, listOf(Color(0xFFEC407A), Color(0xFFC2185B)), Icons.Default.Draw)
        )
    }

    var orderedLearningItems by remember { mutableStateOf<List<LearningItem>>(emptyList()) }
    var isReorderMode by remember { mutableStateOf(false) }
    var isOrderChanged by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedOrder = learningOrderRepository.learningOrderFlow.first()
        orderedLearningItems = if (savedOrder.isEmpty()) {
            allLearningItems
        } else {
            val itemsMap = allLearningItems.associateBy { it.id }
            val loadedItems = savedOrder.mapNotNull { itemsMap[it] }
            // If new items (like Roman) are missing from saved order, add them at the end
            val missingItems = allLearningItems.filter { it.id !in savedOrder }
            val finalItems = loadedItems + missingItems

            isOrderChanged = finalItems.map { it.id } != allLearningItems.map { it.id }
            finalItems
        }
    }

    val onMoveUp = { index: Int ->
        if (index > 0) {
            val newList = orderedLearningItems.toMutableList().apply {
                Collections.swap(this, index, index - 1)
            }
            orderedLearningItems = newList
            isOrderChanged = true
        }
    }

    val onMoveDown = { index: Int ->
        if (index < orderedLearningItems.size - 1) {
            val newList = orderedLearningItems.toMutableList().apply {
                Collections.swap(this, index, index + 1)
            }
            orderedLearningItems = newList
            isOrderChanged = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.learning_category_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                },
                actions = {
                    AnimatedVisibility(visible = isReorderMode && isOrderChanged) {
                        IconButton(onClick = {
                            scope.launch { learningOrderRepository.clearOrder() }
                            orderedLearningItems = allLearningItems
                            isOrderChanged = false
                        }) {
                            Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.reset_order_description))
                        }
                    }

                    IconButton(onClick = {
                        isReorderMode = !isReorderMode
                        if (!isReorderMode && isOrderChanged) {
                            scope.launch { learningOrderRepository.saveOrder(orderedLearningItems.map { it.id }) }
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
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(orderedLearningItems, key = { _, item -> item.id }) { index, item ->
                Box(modifier = Modifier.animateItemPlacement(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )) {
                    LearningItemCard(
                        item = item,
                        isReorderMode = isReorderMode,
                        onClick = { if (!isReorderMode) navController.navigate(item.route) },
                        index = index,
                        totalItemCount = orderedLearningItems.size,
                        onMoveUp = { onMoveUp(index) },
                        onMoveDown = { onMoveDown(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningItemCard(
    item: LearningItem,
    isReorderMode: Boolean,
    onClick: () -> Unit,
    index: Int,
    totalItemCount: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush = Brush.horizontalGradient(colors = item.gradient))
            .clickable(onClick = onClick, enabled = !isReorderMode)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = isReorderMode) {
                Column {
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

            Text(
                text = stringResource(id = item.titleResId),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )

            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
            }
        }
    }
}