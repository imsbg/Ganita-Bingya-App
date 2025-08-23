// MainActivity.kt

package com.sandeep.ganitabigyan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.sandeep.ganitabigyan.ui.theme.GanitaBigyanTheme

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
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    private fun askPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // CHANGE: New function to enable high refresh rate
    private fun enableHighRefreshRate() {
        // This feature is only available on Android 11 (R) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display: Display? = display
            if (display != null) {
                // Get all supported refresh rates
                val supportedModes = display.supportedModes
                var highestRefreshRate = 60f // Default

                // Find the highest supported refresh rate
                for (mode in supportedModes) {
                    if (mode.refreshRate > highestRefreshRate) {
                        highestRefreshRate = mode.refreshRate
                    }
                }

                // If a higher rate is found, apply it
                if (highestRefreshRate > 60f) {
                    val params = window.attributes
                    params.preferredRefreshRate = highestRefreshRate
                    window.attributes = params
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // CHANGE: Call the new function on startup
        enableHighRefreshRate()

        askPermissions()
        scheduleReminders(applicationContext)

        setContent {
            GanitaBigyanTheme {
                val navController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavHost(
                        navController = navController,
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }
}