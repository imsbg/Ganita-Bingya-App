// FILE: app/src/main/java/com/sandeep/ganitabigyan/NavGraph.kt

package com.sandeep.ganitabigyan

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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

object AppDestinations { const val SPLASH_ROUTE = "splash"; const val WELCOME_ROUTE = "welcome"; const val HOME_ROUTE = "home"; const val GAME_ROUTE = "game"; const val ABOUT_ROUTE = "about"; const val SCORE_HISTORY_ROUTE = "score_history"; const val SETTINGS_ROUTE = "settings"; const val CALCULATOR_ROUTE = "calculator"; const val PANIKIA_LIST_ROUTE = "panikia_list"; const val PANIKIA_DETAIL_ROUTE = "panikia_detail/{tableNumber}?view={viewType}"; const val NUMBERS_ROUTE = "numbers"; const val DRAWING_ROUTE = "drawing"; const val DRAWING_HISTORY_ROUTE = "drawing_history"; const val CHANGELOG_ROUTE = "changelog" }

@Composable
fun NavGraph(gameViewModel: GameViewModel, modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppDestinations.SPLASH_ROUTE, modifier = modifier) {
        composable(AppDestinations.SPLASH_ROUTE) { SplashScreen(navController = navController) }
        composable(AppDestinations.WELCOME_ROUTE) { WelcomeScreen(onStartClick = { navController.navigate(AppDestinations.HOME_ROUTE) { popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true } } }) }
        composable(route = AppDestinations.HOME_ROUTE) { HomeScreen(navController = navController) }
        composable(route = AppDestinations.GAME_ROUTE) { GameScreen(viewModel = gameViewModel, onNavigateBack = { navController.popBackStack() }, onNavigateToScore = { navController.navigate(AppDestinations.SCORE_HISTORY_ROUTE) }) }
        composable(route = AppDestinations.SCORE_HISTORY_ROUTE) { ScoreHistoryScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.SETTINGS_ROUTE) { SettingsScreen(navController = navController) }
        composable(route = AppDestinations.ABOUT_ROUTE) { AboutScreen(navController = navController, onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.CHANGELOG_ROUTE) { ChangelogScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.CALCULATOR_ROUTE) { CalculatorScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.PANIKIA_LIST_ROUTE) { PanikiaListScreen(onTableClick = { tableNumber -> navController.navigate("panikia_detail/$tableNumber") }, onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.PANIKIA_DETAIL_ROUTE, arguments = listOf(navArgument("tableNumber") { type = NavType.IntType }, navArgument("viewType") { type = NavType.StringType; nullable = true; defaultValue = "number" })) { backStackEntry -> val tableNumber = backStackEntry.arguments?.getInt("tableNumber") ?: 2; val viewType = backStackEntry.arguments?.getString("viewType"); PanikiaDetailScreen(tableNumber = tableNumber, initialView = viewType, onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.NUMBERS_ROUTE) { NumberScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(route = AppDestinations.DRAWING_ROUTE) { DrawingScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToHistory = { navController.navigate(AppDestinations.DRAWING_HISTORY_ROUTE) }) }
        composable(route = AppDestinations.DRAWING_HISTORY_ROUTE) { DrawingHistoryScreen(onNavigateBack = { navController.popBackStack() }) }
    }
}


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
        // <<< FIX: Corrected typo from checkForupdates to checkForUpdates >>>
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

        val data = intent?.data
        val isAppLink = data?.scheme == "https" && data.host == "ganitabingya.netlify.app"
        val isCustomScheme = data?.scheme == "ganitabingya" && data.host == "open"

        if (isAppLink || isCustomScheme) {
            val queryParams = data.queryParameterNames; var finalRoute: String? = null; if (queryParams.contains("play")) finalRoute = AppDestinations.GAME_ROUTE; else if (queryParams.contains("numbers")) finalRoute = AppDestinations.NUMBERS_ROUTE; else if (queryParams.contains("ap")) finalRoute = AppDestinations.DRAWING_ROUTE; else if (queryParams.contains("calculator")) finalRoute = AppDestinations.CALCULATOR_ROUTE; else if (queryParams.contains("panikia")) { val value = data.getQueryParameter("panikia"); val number = value?.toIntOrNull() ?: convertWordToNumber(value); if (number != null) { val viewType = if (value?.toIntOrNull() != null) "number" else "word"; finalRoute = "panikia_detail/$number?view=$viewType" } }; if (finalRoute != null) { navController.navigate(AppDestinations.HOME_ROUTE) { popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true } }; navController.navigate(finalRoute); intent?.data = null; return@LaunchedEffect }
        }

        if (data?.path?.endsWith("qna.gba") == true || data?.path?.endsWith("lifetime_score.gba") == true) { navController.navigate(AppDestinations.SCORE_HISTORY_ROUTE) { popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true } }; intent?.data = null; return@LaunchedEffect }
        val hasCompletedWelcome = dataStore.hasCompletedWelcome.first()
        val destination = if (hasCompletedWelcome) AppDestinations.HOME_ROUTE else AppDestinations.WELCOME_ROUTE
        navController.navigate(destination) { popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = splashConfig?.backgroundBrush ?: SolidColor(Color.White))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = splashConfig?.logoPath ?: R.drawable.logo,
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