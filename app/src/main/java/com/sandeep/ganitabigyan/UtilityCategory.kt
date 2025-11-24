// FILE: app/src/main/java/com/sandeep/ganitabigyan/UtilityCategory.kt
package com.sandeep.ganitabigyan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Title // Icon for Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private data class UtilityCardItem(
    val titleResId: Int,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityCategoryScreen(navController: NavController) {
    val utilityItems = listOf(
        UtilityCardItem(
            titleResId = R.string.menu_calculator,
            icon = Icons.Default.Calculate,
            color = Color(0xFF4DB6AC), // Teal
            route = AppDestinations.CALCULATOR_ROUTE
        ),
        UtilityCardItem(
            titleResId = R.string.unit_converter_title,
            icon = Icons.Default.SwapHoriz,
            color = Color(0xFF7E57C2), // Purple
            route = AppDestinations.UNIT_CONVERTER_ROUTE
        ),
        // <<< NEW CARD ADDED HERE >>>
        UtilityCardItem(
            titleResId = R.string.menu_number_to_text, // Defined in strings.xml
            icon = Icons.Default.Title, // Represents Text
            color = Color(0xFFF57C00), // Orange
            route = AppDestinations.NUMBER_TO_TEXT_ROUTE // Make sure this exists in NavGraph
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.home_category_utility),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(utilityItems) { item ->
                UtilityCard(item = item, onClick = { navController.navigate(item.route) })
            }
        }
    }
}

@Composable
private fun UtilityCard(item: UtilityCardItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = item.color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = item.titleResId),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = item.color,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
                    .padding(8.dp)
            )
        }
    }
}