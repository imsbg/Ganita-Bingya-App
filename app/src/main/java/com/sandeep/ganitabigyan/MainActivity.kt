package com.sandeep.ganitabigyan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sandeep.ganitabigyan.ui.theme.GanitaBigyanTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

// Helper function to create the correct context. This keeps MainActivity cleaner.
private fun getLocalizedContext(baseContext: Context): Context {
    val dataStore = SettingsDataStore(baseContext)
    val languageCode = runBlocking { dataStore.language.first() }

    if (languageCode.isEmpty() || languageCode == "system") {
        // If no language is saved or it's set to system default, return the base context as is.
        return baseContext
    }

    val locale = Locale(languageCode)
    Locale.setDefault(locale)

    val config = Configuration(baseContext.resources.configuration)
    config.setLocale(locale)

    return baseContext.createConfigurationContext(config)
}


class MainActivity : ComponentActivity() {

    // --- THIS IS THE CRITICAL FIX ---
    // The `attachBaseContext` method is called by the system BEFORE `onCreate`.
    // This is the earliest and most reliable place to override the app's language.
    override fun attachBaseContext(newBase: Context) {
        val localizedContext = getLocalizedContext(newBase)
        super.attachBaseContext(localizedContext)
    }

    private val gameViewModel: GameViewModel by viewModels { GameViewModelFactory(this.applicationContext) }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Handle permission result if needed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We also call this modern API to ensure full compatibility with Android 13+ per-app locales.
        // This reinforces the change made in attachBaseContext.
        val dataStore = SettingsDataStore(this)
        val langCode = runBlocking { dataStore.language.first() }
        if (langCode.isNotEmpty() && langCode != "system") {
            val appLocale = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        installSplashScreen()
        askNotificationPermission()

        setContent {
            val settingsViewModel: SettingsViewModel by viewModels()
            val languageCode by settingsViewModel.language.collectAsState()

            // The `key` is still important. It forces Compose to redraw the UI
            // after a restart, ensuring it uses the new language configuration.
            val currentLocaleCode = if (languageCode == "system") {
                Locale.getDefault().language
            } else {
                languageCode
            }

            key(currentLocaleCode) {
                GanitaBigyanTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        NavGraph(gameViewModel = gameViewModel)
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}