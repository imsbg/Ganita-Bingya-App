package com.sandeep.ganitabigyan

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.coroutines.delay

object AppDestinations {
    const val SPLASH_ROUTE = "splash"
    const val GAME_ROUTE = "game"
    const val ABOUT_ROUTE = "about"
    const val HISTORY_ROUTE = "history"
    const val SCORE_ROUTE = "score"
    const val CALCULATOR_ROUTE = "calculator"
    const val PANIKIA_LIST_ROUTE = "panikia_list"
    const val PANIKIA_DETAIL_ROUTE = "panikia_detail/{tableNumber}"
    const val NUMBERS_ROUTE = "numbers" // <-- ADD THIS LINE
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    gameViewModel: GameViewModel,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH_ROUTE,
        modifier = modifier
    ) {
        // ... (all your existing composable routes for splash, game, history, etc. remain unchanged)

        composable(AppDestinations.SPLASH_ROUTE) {
            val context = LocalContext.current
            SplashScreen(onTimeout = {
                val intent = (context as? Activity)?.intent
                val data = intent?.data

                if (data?.path?.endsWith("qna.gba") == true) {
                    navController.navigate(AppDestinations.HISTORY_ROUTE) {
                        popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                } else if (data?.path?.endsWith("lifetime_score.gba") == true) {
                    navController.navigate(AppDestinations.SCORE_ROUTE) {
                        popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                } else {
                    navController.navigate(AppDestinations.GAME_ROUTE) {
                        popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                }
                intent?.data = null
            })
        }
        composable(
            route = AppDestinations.GAME_ROUTE,
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            GameScreen(
                viewModel = gameViewModel,
                onMenuClick = onMenuClick,
                onNavigateToScore = { navController.navigate(AppDestinations.SCORE_ROUTE) }
            )
        }
        composable(
            route = AppDestinations.HISTORY_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) {
            HistoryScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestinations.SCORE_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) {
            ScoreScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestinations.ABOUT_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = AppDestinations.CALCULATOR_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) {
            CalculatorScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = AppDestinations.PANIKIA_LIST_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) {
            PanikiaListScreen(
                onTableClick = { tableNumber ->
                    navController.navigate("panikia_detail/$tableNumber")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppDestinations.PANIKIA_DETAIL_ROUTE,
            arguments = listOf(navArgument("tableNumber") { type = NavType.IntType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) { backStackEntry ->
            val tableNumber = backStackEntry.arguments?.getInt("tableNumber") ?: 2
            PanikiaDetailScreen(
                tableNumber = tableNumber,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // v-- THIS IS THE NEW NAVIGATION BLOCK --v
        composable(
            route = AppDestinations.NUMBERS_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, animationSpec = tween(500)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, animationSpec = tween(500)) }
        ) {
            NumberScreen(onNavigateBack = { navController.popBackStack() })
        }
        // ^-- THIS IS THE NEW NAVIGATION BLOCK --^
    }
}

// ... (SplashScreen composable remains unchanged)
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var currentText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(200)
        currentText = "ଜୟ ଜଗନ୍ନାଥ"
        delay(2500)
        currentText = "ଗଣିତ ବିଜ୍ଞକୁ ସ୍ୱାଗତ"
        delay(2500)
        currentText = ""
        delay(500)
        onTimeout()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            AnimatedContent(
                targetState = currentText,
                transitionSpec = {
                    fadeIn(animationSpec = tween(1000)) togetherWith
                            fadeOut(animationSpec = tween(1000))
                },
                label = "Splash Text Animation"
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.displayMedium,
                )
            }
        }
    }
}