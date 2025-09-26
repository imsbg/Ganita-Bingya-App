// FILE: app/src/main/java/com/sandeep/ganitabigyan/LearningCategoryScreen.kt
// PASTE THIS ENTIRE CODE INTO THE NEW FILE

package com.sandeep.ganitabigyan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private data class LearningItem(
    val titleResId: Int,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningCategoryScreen(navController: NavController) {
    val learningItems = listOf(
        LearningItem(R.string.menu_panikia, AppDestinations.PANIKIA_LIST_ROUTE, listOf(Color(0xFFBA68C8), Color(0xFF9C27B0)), Icons.Default.MenuBook),
        LearningItem(R.string.menu_numbers, AppDestinations.NUMBERS_ROUTE, listOf(Color(0xFFE57373), Color(0xFFF44336)), Icons.Default.LooksOne),
        LearningItem(R.string.menu_drawing_pad, AppDestinations.DRAWING_ROUTE, listOf(Color(0xFF4DB6AC), Color(0xFF009688)), Icons.Default.Draw)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.learning_category_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
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
            items(learningItems) { item ->
                LearningItemCard(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun LearningItemCard(item: LearningItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush = Brush.horizontalGradient(colors = item.gradient))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(id = item.titleResId), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
            }
        }
    }
}