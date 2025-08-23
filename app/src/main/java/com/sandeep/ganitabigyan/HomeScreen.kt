// HomeScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private data class HomeMenuItem(
    val title: String,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector? = null,
    val textIcon: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val gradientOrange = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
    val gradientBlue = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
    val gradientPurple = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2))
    val gradientRed = listOf(Color(0xFFEF5350), Color(0xFFC62828))
    val gradientTeal = listOf(Color(0xFF26A69A), Color(0xFF00796B))
    val gradientCyan = listOf(Color(0xFF26C6DA), Color(0xFF0097A7))


    val menuItems = listOf(
        // The text is "୫+୪" (5+4) as requested
        HomeMenuItem("ଖେଳ ଆରମ୍ଭ କରନ୍ତୁ", AppDestinations.GAME_ROUTE, gradientOrange, textIcon = "୫+୪"),
        HomeMenuItem("ଆପଣଙ୍କ ପ୍ରଗତି", AppDestinations.SCORE_HISTORY_ROUTE, gradientBlue, icon = Icons.Default.Insights),
        HomeMenuItem("ପଣିକିଆ", AppDestinations.PANIKIA_LIST_ROUTE, gradientPurple, icon = Icons.Default.MenuBook),
        // The text is "୪୫" (45) as requested
        HomeMenuItem("ସଙ୍ଖ୍ୟା", AppDestinations.NUMBERS_ROUTE, gradientRed, textIcon = "୪୫"),
        HomeMenuItem("ଅଙ୍କନ ପ୍ୟାଡ୍", AppDestinations.DRAWING_ROUTE, gradientTeal, icon = Icons.Default.Draw),
        HomeMenuItem("କ୍ୟାଲକୁଲେଟର", AppDestinations.CALCULATOR_ROUTE, gradientCyan, icon = Icons.Default.Calculate)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ଗଣିତ ବିଜ୍ଞ", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            items(menuItems) { item ->
                MenuItemCard(
                    item = item,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(item: HomeMenuItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(brush = Brush.horizontalGradient(colors = item.gradient))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                } else if (item.textIcon != null) {
                    // FINAL FIX: More precise font size adjustment
                    val fontSize = when {
                        item.textIcon.length > 2 -> 36.sp // Reduced size for "୫+୪" to guarantee it fits
                        else -> 48.sp // Perfect size for "୪୫"
                    }
                    Text(
                        text = item.textIcon,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1 // Ensure it stays on one line
                    )
                }
            }
        }
    }
}