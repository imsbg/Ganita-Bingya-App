package com.sandeep.ganitabigyan

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.sandeep.ganitabigyan.ui.theme.GanitaBigyanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return GameViewModel(applicationContext) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askNotificationPermission()
        setContent {
            GanitaBigyanTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val gameState by gameViewModel.gameState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            AppDrawerContent(
                                drawerState = drawerState,
                                scope = scope,
                                isAutoScrollEnabled = gameState.isAutoScrollEnabled,
                                onAutoScrollToggled = { isEnabled -> gameViewModel.toggleAutoScroll(isEnabled) },
                                onTimedChallengeClick = { gameViewModel.requestTimedChallengeDialog() },
                                onNavigateToHistory = { navController.navigate(AppDestinations.HISTORY_ROUTE) },
                                onNavigateToScore = { navController.navigate(AppDestinations.SCORE_ROUTE) },
                                onNavigateToAbout = { navController.navigate(AppDestinations.ABOUT_ROUTE) },
                                onNavigateToCalculator = { navController.navigate(AppDestinations.CALCULATOR_ROUTE) } // <-- Added this line
                            )
                        }
                    ) {
                        AppNavHost(
                            navController = navController,
                            gameViewModel = gameViewModel,
                            onMenuClick = {
                                scope.launch { drawerState.open() }
                            }
                        )
                    }
                }
            }
        }
    }
}