// HomeScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController

// --- DEFINE APP BAR HEIGHTS FOR ACCURATE CALCULATIONS ---
private val CollapsedAppBarHeight = 64.dp
private val ExpandedAppBarHeight = 152.dp // Standard height for Material3 LargeTopAppBar


// Data class to represent each item in the home screen menu
private data class HomeMenuItem(
    val title: String,
    val subtitle: String? = null,
    val route: String,
    val gradient: List<Color>,
    val icon: ImageVector? = null,
    val textIcon: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val gradientOrange = listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
    val gradientBlue = listOf(Color(0xFF64B5F6), Color(0xFF2196F3))
    val gradientPurple = listOf(Color(0xFFBA68C8), Color(0xFF9C27B0))
    val gradientRed = listOf(Color(0xFFE57373), Color(0xFFF44336))
    val gradientTeal = listOf(Color(0xFF4DB6AC), Color(0xFF009688))
    val gradientCyan = listOf(Color(0xFF4DD0E1), Color(0xFF00BCD4))

    val menuItems = listOf(
        HomeMenuItem("ଖେଳ ଆରମ୍ଭ କରନ୍ତୁ", "୫+୪, ୪-୨", AppDestinations.GAME_ROUTE, gradientOrange, Icons.Default.PlayArrow),
        HomeMenuItem("ଆପଣଙ୍କ ପ୍ରଗତି", "କେତେ ଠିକ? କେତେ ଭୁଲ?", AppDestinations.SCORE_HISTORY_ROUTE, gradientBlue, Icons.Default.Insights),
        HomeMenuItem("ପଣିକିଆ", "ଦୁଇ କେ ଦୁଇ", AppDestinations.PANIKIA_LIST_ROUTE, gradientPurple, Icons.Default.MenuBook),
        HomeMenuItem("ସଙ୍ଖ୍ୟା", "୦ ୧ ୨ ୩ ୪ ୫ ୬ ୭ ୮ ୯", AppDestinations.NUMBERS_ROUTE, gradientRed, textIcon = "୪୫"),
        HomeMenuItem("ଅଙ୍କନ ପ୍ୟାଡ୍", null, AppDestinations.DRAWING_ROUTE, gradientTeal, Icons.Default.Draw),
        HomeMenuItem("କ୍ୟାଲକୁଲେଟର", null, AppDestinations.CALCULATOR_ROUTE, gradientCyan, Icons.Default.Calculate)
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = { /* Empty */ },
                    actions = {
                        IconButton(onClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp).copy(alpha = 0.9f)
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color(0xFFF0F4F8)),
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

        CollapsingToolbar(scrollBehavior = scrollBehavior)
    }
}


// --- REWRITTEN FOR "TOP LEFT CORNER" ANIMATION (TEXT ONLY) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingToolbar(scrollBehavior: TopAppBarScrollBehavior) {
    var titleSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Define the final padding for the collapsed state
    val startPadding = 16.dp

    val collapsedHeightPx = with(density) { CollapsedAppBarHeight.toPx() }
    val expandedHeightPx = with(density) { ExpandedAppBarHeight.toPx() }
    val startPaddingPx = with(density) { startPadding.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ExpandedAppBarHeight + topPadding)
            .padding(top = topPadding),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            " ଗଣିତ ବିଜ୍ଞ",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold), // Slightly larger for better presence
            maxLines = 1,
            modifier = Modifier
                .onSizeChanged {
                    if (titleSize != it) titleSize = it
                }
                .graphicsLayer {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction

                    // --- SCALE ---
                    // Scale down to a more standard title size
                    val startScale = 1.0f
                    val endScale = 0.7f
                    val currentScale = lerp(startScale, endScale, collapsedFraction)

                    // --- X-AXIS TRANSLATION (HORIZONTAL) ---
                    // Animate from center to the left padding. THIS IS THE KEY CHANGE.
                    val startTranslationX = (size.width / 2) - (titleSize.width / 2)
                    val endTranslationX = startPaddingPx
                    val currentTranslationX = lerp(startTranslationX, endTranslationX, collapsedFraction)

                    // --- Y-AXIS TRANSLATION (VERTICAL) ---
                    // Animate from the bottom of the expanded area to the center of the collapsed area.
                    val startTranslationY = expandedHeightPx - titleSize.height
                    val endTranslationY = (collapsedHeightPx / 2) - (titleSize.height / 2)
                    val currentTranslationY = lerp(startTranslationY, endTranslationY, collapsedFraction)

                    // Apply all transformations
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = currentTranslationX
                    translationY = currentTranslationY
                }
        )
    }
}


@Composable
private fun MenuItemCard(item: HomeMenuItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(28.dp))
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    fontSize = 26.sp
                )
                item.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 20.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                } else if (item.textIcon != null) {
                    Text(
                        text = item.textIcon,
                        color = Color.White,
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 1.em,
                        maxLines = 2
                    )
                }
            }
        }
    }
}