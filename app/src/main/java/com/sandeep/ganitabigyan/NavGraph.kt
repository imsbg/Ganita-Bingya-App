// FILE: app/src/main/java/com/sandeep/ganitabigyan/NavGraph.kt

package com.sandeep.ganitabigyan

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.sandeep.ganitabigyan.utils.DynamicAssetManager
import com.sandeep.ganitabigyan.utils.SplashConfig
import com.sandeep.ganitabigyan.widget.GanitaWidgetReceiver.Companion.WIDGET_DESTINATION_KEY
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import com.sandeep.ganitabigyan.VersionManager

object AppDestinations {
    const val SPLASH_ROUTE = "splash"
    const val WELCOME_ROUTE = "welcome"
    const val HOME_ROUTE = "home"
    const val GAME_ROUTE = "game"
    const val VISUAL_GAME_ROUTE = "visual_game"
    const val LOGIC_GAME_ROUTE = "logic_game"
    const val ABOUT_ROUTE = "about"
    const val SCORE_HISTORY_ROUTE = "score_history"
    const val SETTINGS_ROUTE = "settings"
    const val LANGUAGE_SELECTION_ROUTE = "language_selection"
    const val THEME_SELECTION_ROUTE = "theme_selection"
    const val CALCULATOR_ROUTE = "calculator"
    const val PANIKIA_LIST_ROUTE = "panikia_list"
    const val PANIKIA_DETAIL_ROUTE = "panikia_detail/{tableNumber}?view={viewType}"
    const val NUMBERS_ROUTE = "numbers"
    const val DRAWING_ROUTE = "drawing"
    const val DRAWING_HISTORY_ROUTE = "drawing_history"
    const val CHANGELOG_ROUTE = "changelog"
    const val GAMES_CATEGORY_ROUTE = "games_category"
    const val LEARNING_CATEGORY_ROUTE = "learning_category"
    const val SUDOKU_ROUTE = "sudoku"
    const val GAME_2048_ROUTE = "game_2048"
    const val FTMN_GAME_ROUTE = "ftmn_game"
    const val SLIDING_BLOCK_PUZZLE_ROUTE = "sliding_block_puzzle"
    const val WORD_PROBLEM_GAME_ROUTE = "word_problem_game"
    const val ORDINALS_ROUTE = "ordinals"
    const val GANANA_ROUTE = "ganana"
    const val UTILITY_CATEGORY_ROUTE = "utility_category"
    const val UNIT_CONVERTER_ROUTE = "unit_converter"
    const val NUMBER_TO_TEXT_ROUTE = "number_to_text"
    const val ROMAN_ROUTE = "roman_screen"
}

// A reusable object for our screen slide animations
object ScreenTransitions {
    private const val DURATION = 350 // Animation speed in milliseconds

    val enter: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(DURATION)
    ) + fadeIn(animationSpec = tween(DURATION))

    val exit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(DURATION)
    ) + fadeOut(animationSpec = tween(DURATION))

    val popEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(DURATION)
    ) + fadeIn(animationSpec = tween(DURATION))

    val popExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DURATION)
    ) + fadeOut(animationSpec = tween(DURATION))
}


@Composable
fun NavGraph(gameViewModel: GameViewModel, modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppDestinations.SPLASH_ROUTE, modifier = modifier) {
        // Screens without custom transitions (Splash, Welcome)
        composable(AppDestinations.SPLASH_ROUTE) { SplashScreen(navController = navController) }
        composable(AppDestinations.WELCOME_ROUTE) {
            WelcomeScreen(onStartClick = {
                navController.navigate(
                    AppDestinations.HOME_ROUTE
                ) { popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true } }
            })
        }

        // Home screen with a simple fade, since its internal animation is the main effect
        composable(
            route = AppDestinations.HOME_ROUTE,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) { HomeScreen(navController = navController) }

        // All other screens get the new slide transition
        val transitions = ScreenTransitions

        composable(
            route = AppDestinations.GAME_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) {
            GameScreen(
                viewModel = gameViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScore = { navController.navigate(AppDestinations.SCORE_HISTORY_ROUTE) })
        }
        composable(
            route = AppDestinations.VISUAL_GAME_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { VisualGameScreen(navController = navController) }
        composable(
            route = AppDestinations.LOGIC_GAME_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { LogicGameScreen(navController = navController) }
        composable(
            route = AppDestinations.SCORE_HISTORY_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { ScoreHistoryScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = AppDestinations.SETTINGS_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { SettingsScreen(navController = navController) }

        composable(
            route = AppDestinations.LANGUAGE_SELECTION_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { LanguageSelectionScreen(navController = navController) }
        composable(
            route = AppDestinations.THEME_SELECTION_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { ThemeSelectionScreen(navController = navController) }
        composable(
            route = AppDestinations.ABOUT_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) {
            AboutScreen(
                navController = navController,
                onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestinations.CHANGELOG_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { ChangelogScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = AppDestinations.CALCULATOR_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { CalculatorScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = AppDestinations.PANIKIA_LIST_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) {
            PanikiaListScreen(
                onTableClick = { tableNumber -> navController.navigate("panikia_detail/$tableNumber") },
                onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestinations.PANIKIA_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("tableNumber") { type = NavType.IntType },
                navArgument("viewType") {
                    type = NavType.StringType; nullable = true; defaultValue = "number"
                }),
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { backStackEntry ->
            val tableNumber = backStackEntry.arguments?.getInt("tableNumber") ?: 2;
            val viewType = backStackEntry.arguments?.getString("viewType"); PanikiaDetailScreen(
            tableNumber = tableNumber,
            initialView = viewType,
            onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestinations.NUMBERS_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { NumberScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = AppDestinations.DRAWING_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) {
            DrawingScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHistory = { navController.navigate(AppDestinations.DRAWING_HISTORY_ROUTE) })
        }
        composable(
            route = AppDestinations.DRAWING_HISTORY_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { DrawingHistoryScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(
            route = AppDestinations.GAMES_CATEGORY_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { GamesCategoryScreen(navController = navController) }
        composable(
            route = AppDestinations.LEARNING_CATEGORY_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { LearningCategoryScreen(navController = navController) }
        composable(
            route = AppDestinations.SUDOKU_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { SudokuScreen(navController = navController) }
        composable(
            route = AppDestinations.FTMN_GAME_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { FTMNScreen(navController = navController) }
        composable(
            route = AppDestinations.SLIDING_BLOCK_PUZZLE_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { SlidingBlockPuzzleScreen(navController = navController) }
        composable(
            route = AppDestinations.WORD_PROBLEM_GAME_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { WordProblemScreen(navController = navController) }
        composable(
            AppDestinations.ORDINALS_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { OrdinalsScreen(navController = navController) }
        composable(
            AppDestinations.GANANA_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { GananaScreen(navController = navController) }
        composable(
            route = AppDestinations.UNIT_CONVERTER_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { UnitConverterScreen(navController = navController) }
        // --- THIS IS THE NEW SCREEN YOU ARE ADDING ---
        composable(
            route = AppDestinations.UTILITY_CATEGORY_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }) { UtilityCategoryScreen(navController = navController) }
// Number to Text Screen with Animations
        composable(
            route = AppDestinations.NUMBER_TO_TEXT_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }
        ) {
            NumberToTextScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Roman Numbers Screen with Animations
        composable(
            route = AppDestinations.ROMAN_ROUTE,
            enterTransition = { transitions.enter },
            exitTransition = { transitions.exit },
            popEnterTransition = { transitions.popEnter },
            popExitTransition = { transitions.popExit }
        ) {
            RomanNumbersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// SplashScreen and other functions remain unchanged
@Composable
fun SplashScreen(navController: NavHostController) {
    var currentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val assetManager = remember { DynamicAssetManager(context) }
    var splashConfig by remember { mutableStateOf<SplashConfig?>(null) }
    val appLogoDesc = stringResource(R.string.app_logo_description)
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (startAnimation) 1f else 0.8f, animationSpec = tween(durationMillis = 1000), label = "logoScale")
    val alpha by animateFloatAsState(targetValue = if (startAnimation) 1f else 0f, animationSpec = tween(durationMillis = 1000), label = "logoAlpha")
    var isFooterVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        VersionManager.handleUpdate(context)
        launch { assetManager.checkForUpdates() }
        splashConfig = assetManager.getSplashConfig()
        startAnimation = true
        delay(200); currentText = splashConfig?.splashText1 ?: ""
        isFooterVisible = true
        delay(1500); currentText = splashConfig?.splashText2 ?: ""
        if (splashConfig?.splashText3?.isNotBlank() == true) {
            delay(1500); currentText = splashConfig!!.splashText3
        }
        delay(1500)
        isFooterVisible = false
        currentText = ""
        delay(300)
        val intent = (context as? Activity)?.intent
        if (intent?.hasExtra(WIDGET_DESTINATION_KEY) == true) {
            val destinationRoute = intent.getStringExtra(WIDGET_DESTINATION_KEY)
            if (destinationRoute != null) {
                navController.navigate(AppDestinations.HOME_ROUTE) { popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true } }
                navController.navigate(destinationRoute)
                intent.removeExtra(WIDGET_DESTINATION_KEY)
                return@LaunchedEffect
            }
        }
        val hasCompletedWelcome = dataStore.hasCompletedWelcome.first()
        val destination = if (hasCompletedWelcome) AppDestinations.HOME_ROUTE else AppDestinations.WELCOME_ROUTE
        navController.navigate(destination) { popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true } }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = if (splashConfig?.backgroundImagePath?.isBlank() == true) splashConfig!!.backgroundBrush else SolidColor(Color.Transparent))
    ) {
        val bgPath = splashConfig?.backgroundImagePath
        if (!bgPath.isNullOrBlank()) {
            val imageFile = File(bgPath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = imageFile,
                    contentDescription = "Festival Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val logoPath = splashConfig?.logoPath
            val logoModel = if (!logoPath.isNullOrBlank() && File(logoPath).exists()) {
                File(logoPath)
            } else {
                R.drawable.logo_splash
            }
            AsyncImage(
                model = logoModel,
                contentDescription = appLogoDesc,
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
                    .alpha(alpha)
            )
            Spacer(modifier = Modifier.height(32.dp))
            AnimatedContent(
                targetState = currentText,
                transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
                label = "Splash Text Animation"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.displayMedium,
                    color = splashConfig?.textColor ?: Color.Black
                )
            }
        }
        AnimatedVisibility(
            visible = isFooterVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Made with ❤️ in Odisha, India",
                    style = MaterialTheme.typography.bodyMedium,
                    color = splashConfig?.textColor ?: Color.Black
                )
                Text(
                    text = "by Sandeep Biswal G",
                    style = MaterialTheme.typography.bodyMedium,
                    color = splashConfig?.textColor ?: Color.Black
                )
            }
        }
    }
}

private fun convertWordToNumber(word: String?): Int? {
    return when (word?.lowercase()) { "one" -> 1; "two" -> 2; "three" -> 3; "four" -> 4; "five" -> 5; "six" -> 6; "seven" -> 7; "eight" -> 8; "nine" -> 9; "ten" -> 10; else -> null }
}