// FILE: app/src/main/java/com/sandeep/ganitabigyan/HomeScreen.kt
// PASTE THIS ENTIRE, FINAL CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

// A single data class for all searchable items
private data class SearchableItem(
    val titleResId: Int,
    val subtitleResId: Int? = null,
    val icon: ImageVector,
    val route: String
)

private data class EnrichedSearchableItem(
    val originalItem: SearchableItem,
    val englishTitle: String,
    val odiaTitle: String
)

private data class CategoryItem(val titleResId: Int, val icon: ImageVector, val color: Color, val route: String)
private data class PopularItem(val titleResId: Int, val imageResId: Int, val color: Color, val route: String)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // ... (All the search logic remains exactly the same) ...
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val allSearchableItems = remember {
        listOf(
            SearchableItem(R.string.menu_start_game, R.string.menu_start_game_desc, Icons.Default.PlayArrow, AppDestinations.GAME_ROUTE),
            SearchableItem(R.string.menu_logic_game, R.string.menu_logic_game_desc, Icons.Default.Psychology, AppDestinations.LOGIC_GAME_ROUTE),
            SearchableItem(R.string.menu_visual_game, R.string.menu_visual_game_desc, Icons.Default.Compare, AppDestinations.VISUAL_GAME_ROUTE),
            SearchableItem(R.string.menu_panikia, subtitleResId = null, Icons.Default.MenuBook, AppDestinations.PANIKIA_LIST_ROUTE),
            SearchableItem(R.string.menu_numbers, subtitleResId = null, Icons.Default.LooksOne, AppDestinations.NUMBERS_ROUTE),
            SearchableItem(R.string.menu_drawing_pad, subtitleResId = null, Icons.Default.Draw, AppDestinations.DRAWING_ROUTE),
            SearchableItem(R.string.menu_calculator, subtitleResId = null, Icons.Default.Calculate, AppDestinations.CALCULATOR_ROUTE),
            SearchableItem(R.string.home_category_score, subtitleResId = null, Icons.Default.Insights, AppDestinations.SCORE_HISTORY_ROUTE)
        )
    }

    val context = LocalContext.current
    val enrichedSearchableList = remember {
        allSearchableItems.map { item ->
            EnrichedSearchableItem(
                originalItem = item,
                englishTitle = getStringForLocale(context, item.titleResId, Locale.ENGLISH),
                odiaTitle = getStringForLocale(context, item.titleResId, Locale("or"))
            )
        }
    }

    val filteredItems = remember(searchQuery, enrichedSearchableList) {
        if (searchQuery.isBlank()) {
            enrichedSearchableList
        } else {
            enrichedSearchableList.filter { enrichedItem ->
                enrichedItem.englishTitle.contains(searchQuery, ignoreCase = true) ||
                        enrichedItem.odiaTitle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AnimatedContent(
        targetState = isSearching,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SearchModeAnimation"
    ) { searching ->
        if (searching) {
            SearchScreen(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onCloseSearch = {
                    isSearching = false
                    searchQuery = ""
                },
                items = filteredItems,
                onItemClick = { route ->
                    navController.navigate(route)
                    isSearching = false
                    searchQuery = ""
                }
            )
        } else {
            MainHomeScreen(
                navController = navController,
                onSearchClick = { isSearching = true }
            )
        }
    }
}

private fun getStringForLocale(context: Context, resId: Int, locale: Locale): String {
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    return context.createConfigurationContext(config).getString(resId)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    items: List<EnrichedSearchableItem>,
    onItemClick: (String) -> Unit
) {
    // ... (SearchScreen composable remains exactly the same) ...
    Scaffold { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(stringResource(id = R.string.home_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onCloseSearch) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { enrichedItem ->
                    SearchItemCard(item = enrichedItem.originalItem, onClick = { onItemClick(enrichedItem.originalItem.route) })
                }
            }
        }
    }
}

@Composable
private fun SearchItemCard(item: SearchableItem, onClick: () -> Unit) {
    // ... (SearchItemCard composable remains exactly the same) ...
    ListItem(
        headlineContent = { Text(stringResource(id = item.titleResId), fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            item.subtitleResId?.let { Text(stringResource(id = it)) }
        },
        leadingContent = {
            Icon(imageVector = item.icon, contentDescription = null)
        },
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainHomeScreen(navController: NavController, onSearchClick: () -> Unit) {
    // ... (Greeting, categories, and popularItemsBase lists remain the same) ...
    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> R.string.home_greeting_morning
            in 12..16 -> R.string.home_greeting_afternoon
            in 17..20 -> R.string.home_greeting_evening
            else -> R.string.home_greeting_night
        }
    }

    val categories = listOf(
        CategoryItem(R.string.home_category_games, Icons.Default.VideogameAsset, Color(0xFFFFA726), AppDestinations.GAMES_CATEGORY_ROUTE),
        CategoryItem(R.string.home_category_learning, Icons.Default.School, Color(0xFF3F51B5), AppDestinations.LEARNING_CATEGORY_ROUTE),
        CategoryItem(R.string.home_category_score, Icons.Default.Insights, Color(0xFF2196F3), AppDestinations.SCORE_HISTORY_ROUTE),
        CategoryItem(R.string.home_category_utility, Icons.Default.Construction, Color(0xFF009688), AppDestinations.CALCULATOR_ROUTE)
    )

    val popularItemsBase = listOf(
        PopularItem(R.string.menu_multiplication_tables, R.drawable.popular_tables, Color(0xFF9C27B0), AppDestinations.PANIKIA_LIST_ROUTE),
        PopularItem(R.string.menu_start_game, R.drawable.popular_math, Color(0xFF4CAF50), AppDestinations.GAME_ROUTE),
        PopularItem(R.string.menu_logic_game, R.drawable.popular_logic, Color(0xFF673AB7), AppDestinations.LOGIC_GAME_ROUTE),
        PopularItem(R.string.menu_visual_game, R.drawable.popular_fruits, Color(0xFFF44336), AppDestinations.VISUAL_GAME_ROUTE),
    )

    // <<< START: NEW CODE FOR SEGMENTED SPEED METER >>>
    val popularItemsState = rememberLazyListState()
    var targetSpeedFraction by remember { mutableFloatStateOf(0f) }

    // This effect detects the raw scroll speed and sets the animation target
    LaunchedEffect(popularItemsState) {
        var lastScrollOffset = popularItemsState.firstVisibleItemScrollOffset
        snapshotFlow { popularItemsState.firstVisibleItemScrollOffset }
            .map { abs(it - lastScrollOffset).also { lastScrollOffset = popularItemsState.firstVisibleItemScrollOffset } }
            .distinctUntilChanged()
            .collect { speed ->
                val maxSpeed = 250f // Adjust this value to change sensitivity
                targetSpeedFraction = (speed.toFloat() / maxSpeed).coerceIn(0f, 1f)
            }
    }

    // This effect resets the target to 0 when scrolling stops
    LaunchedEffect(popularItemsState.isScrollInProgress) {
        if (!popularItemsState.isScrollInProgress) {
            targetSpeedFraction = 0f
        }
    }

    // This is the SMOOTHED value that the UI will use. It animates towards the target.
    val animatedSpeedFraction by animateFloatAsState(
        targetValue = targetSpeedFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "smoothSpeedFraction"
    )

    // Animate the height of the bar container
    val animatedContainerHeight by animateDpAsState(
        targetValue = if (popularItemsState.isScrollInProgress || animatedSpeedFraction > 0.01f) 8.dp else 0.dp,
        label = "containerHeightAnimation"
    )

    // Define the colors for our 20 segments
    val totalLines = 20
    val speedColors = remember {
        List(totalLines) { index ->
            when (index) {
                in 0..7 -> Color.Green.copy(alpha = 0.8f)      // 8 Green lines
                in 8..13 -> Color.Yellow.copy(alpha = 0.8f)    // 6 Yellow lines
                in 14..17 -> Color(0xFFFFA500).copy(alpha = 0.8f) // 4 Orange lines
                else -> Color.Red.copy(alpha = 0.8f)          // 2 Red lines
            }
        }
    }

    // Calculate how many lines should be visible based on the smoothed speed
    val animatedVisibleLines = animatedSpeedFraction * totalLines
    // <<< END: NEW CODE FOR SEGMENTED SPEED METER >>>

    Scaffold( /* ... Scaffold content is unchanged ... */
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.home_search_button_desc))
                    }
                    IconButton(onClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_button_description))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            // ... (All other items in the LazyColumn are unchanged) ...
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = R.string.home_greeting_hello), style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(id = greeting), style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 24.dp))
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoryCard(item = categories[0], modifier = Modifier.weight(1f), onClick = { navController.navigate(categories[0].route) })
                        CategoryCard(item = categories[1], modifier = Modifier.weight(1f), onClick = { navController.navigate(categories[1].route) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoryCard(item = categories[2], modifier = Modifier.weight(1f), onClick = { navController.navigate(categories[2].route) })
                        CategoryCard(item = categories[3], modifier = Modifier.weight(1f), onClick = { navController.navigate(categories[3].route) })
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.home_popular_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                )
            }

            item {
                LazyRow(
                    state = popularItemsState,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(
                        count = Int.MAX_VALUE,
                        key = { index -> index }
                    ) { index ->
                        val item = popularItemsBase[index % popularItemsBase.size]
                        PopularCard(item = item, onClick = { navController.navigate(item.route) })
                    }
                }
            }

            // <<< NEW, REPLACED CODE FOR THE SEGMENTED SPEED METER >>>
            item {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 32.dp)
                        .fillMaxWidth()
                        .height(animatedContainerHeight),
                    horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between each line segment
                ) {
                    repeat(totalLines) { index ->
                        // Calculate the opacity for this specific line segment
                        val opacity = when {
                            // If this line is fully visible
                            index < animatedVisibleLines.toInt() -> 1f
                            // If this is the "frontier" line that is fading in
                            index == animatedVisibleLines.toInt() -> animatedVisibleLines - index
                            // If this line is not yet visible
                            else -> 0f
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f) // Each line takes up equal space
                                .fillMaxHeight()
                                .alpha(opacity) // Apply the calculated fade
                                .clip(RoundedCornerShape(2.dp))
                                .background(speedColors[index]) // Set the color for this segment
                        )
                    }
                }
            }
        }
    }
}

// ... (CategoryCard and PopularCard composables are unchanged) ...
@Composable
private fun CategoryCard(item: CategoryItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.aspectRatio(1.5f).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(8.dp)
            )
            Text(
                text = stringResource(id = item.titleResId),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun PopularCard(item: PopularItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).height(200.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.2f))
    ) {
        Column {
            Image(
                painter = painterResource(id = item.imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            )
            Text(
                text = stringResource(id = item.titleResId),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}