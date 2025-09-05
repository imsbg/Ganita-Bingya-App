// FILE: app/src/main/java/com/sandeep/ganitabigyan/MainActivity.kt
// VERSION: FINAL - Schedules the new background worker for dynamic assets.

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
// <<< NEW IMPORTS for WorkManager >>>
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sandeep.ganitabigyan.ui.theme.GanitaBigyanTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.concurrent.TimeUnit

private fun getLocalizedContext(baseContext: Context): Context {
    val dataStore = SettingsDataStore(baseContext)
    val languageCode = runBlocking { dataStore.language.first() }
    if (languageCode.isEmpty() || languageCode == "system") { return baseContext }
    val locale = Locale(languageCode); Locale.setDefault(locale)
    val config = Configuration(baseContext.resources.configuration); config.setLocale(locale)
    return baseContext.createConfigurationContext(config)
}


class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val localizedContext = getLocalizedContext(newBase)
        super.attachBaseContext(localizedContext)
    }

    private val gameViewModel: GameViewModel by viewModels { GameViewModelFactory(this.applicationContext) }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStore = SettingsDataStore(this)
        val langCode = runBlocking { dataStore.language.first() }
        if (langCode.isNotEmpty() && langCode != "system") {
            val appLocale = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        installSplashScreen()
        askNotificationPermission()

        // <<< THIS IS THE NEW CODE BLOCK >>>
        // Schedule the background tasks when the app starts.
        scheduleBackgroundTasks()

        setContent {
            val settingsViewModel: SettingsViewModel by viewModels()
            val languageCode by settingsViewModel.language.collectAsState()
            val currentLocaleCode = if (languageCode == "system") Locale.getDefault().language else languageCode
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

    // <<< THIS IS THE NEW FUNCTION >>>
    private fun scheduleBackgroundTasks() {
        // 1. Re-schedule the daily reminders to ensure they are always set correctly.
        scheduleReminders(this)

        // 2. Schedule the new worker to check for dynamic assets.
        val assetUpdateRequest = PeriodicWorkRequestBuilder<DynamicAssetWorker>(
            // We can check for a new logo/background every 12 hours.
            // Android will optimize this to save battery.
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dynamic_asset_update_worker",
            ExistingPeriodicWorkPolicy.KEEP, // KEEP means if a worker is already scheduled, don't replace it.
            assetUpdateRequest
        )
    }
}