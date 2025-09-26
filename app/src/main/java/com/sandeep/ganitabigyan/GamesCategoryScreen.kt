// FILE: app/src/main/java/com/sandeep/ganitabigyan/GamesCategoryScreen.kt
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
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private data class GameItem(
    val titleResId: Int,
    val subtitleResId: Int,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesCategoryScreen(navController: NavController) {
    val gameItems = listOf(
        GameItem(R.string.menu_start_game, R.string.menu_start_game_desc, AppDestinations.GAME_ROUTE, listOf(Color(0xFFFFB74D), Color(0xFFFF9800)), Icons.Default.PlayArrow),
        GameItem(R.string.menu_logic_game, R.string.menu_logic_game_desc, AppDestinations.LOGIC_GAME_ROUTE, listOf(Color(0xFF7986CB), Color(0xFF3F51B5)), Icons.Default.Psychology),
        GameItem(R.string.menu_visual_game, R.string.menu_visual_game_desc, AppDestinations.VISUAL_GAME_ROUTE, listOf(Color(0xFF81C784), Color(0xFF4CAF50)), Icons.Default.Compare)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.games_category_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gameItems) { item ->
                GameItemCard(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun GameItemCard(item: GameItem, onClick: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(id = item.titleResId), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text(stringResource(id = item.subtitleResId), style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
            }
            Box(
                modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = item.icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.White)
            }
        }
    }
}