// FILE: app/src/main/java/com/sandeep/ganitabigyan/ui/theme/Theme.kt

package com.sandeep.ganitabigyan.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import com.sandeep.ganitabigyan.AppTheme
import com.sandeep.ganitabigyan.SettingsDataStore

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

fun createCustomColorScheme(seedColor: Color, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = Color(ColorUtils.blendARGB(seedColor.toArgb(), Color.White.toArgb(), 0.2f)),
            secondary = Color(ColorUtils.blendARGB(seedColor.toArgb(), Color.White.toArgb(), 0.3f)),
            tertiary = Color(ColorUtils.blendARGB(seedColor.toArgb(), Color.Gray.toArgb(), 0.3f))
        )
    } else {
        lightColorScheme(
            primary = seedColor,
            secondary = Color(ColorUtils.blendARGB(seedColor.toArgb(), Color.Black.toArgb(), 0.2f)),
            tertiary = Color(ColorUtils.blendARGB(seedColor.toArgb(), Color.Gray.toArgb(), 0.1f))
        )
    }
}

@Composable
fun GanitaBigyanTheme(dynamicColor: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val themePreference by dataStore.themePreference.collectAsState(initial = AppTheme.SYSTEM)
    val customColorHex by dataStore.customThemeColor.collectAsState(initial = "#6750A4")

    val systemIsDark = isSystemInDarkTheme()

    val colorScheme = when (themePreference) {
        // <<< THE MAIN FIX IS HERE >>>
        AppTheme.LIGHT -> {
            // If user selects Light on Android 12+, use Dynamic Color. Otherwise, use the standard Light theme.
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context)
            } else {
                LightColorScheme
            }
        }
        AppTheme.DARK -> DarkColorScheme
        AppTheme.AMOLED -> AmoledColorScheme
        AppTheme.CUSTOM -> {
            try {
                val customColor = Color(android.graphics.Color.parseColor(customColorHex))
                createCustomColorScheme(customColor, systemIsDark)
            } catch (e: Exception) {
                if (systemIsDark) DarkColorScheme else LightColorScheme // Fallback
            }
        }
        else -> { // System Default
            // The System Default option will also correctly use Dynamic Color on modern devices.
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (systemIsDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (systemIsDark) DarkColorScheme else LightColorScheme
            }
        }
    }

    val finalIsDarkTheme = when(themePreference) {
        AppTheme.LIGHT -> false // Light is always light.
        AppTheme.DARK, AppTheme.AMOLED -> true // Dark and AMOLED are always dark.
        // Custom and System follow the phone's setting for things like status bar icons.
        AppTheme.CUSTOM -> systemIsDark
        else -> systemIsDark
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !finalIsDarkTheme
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}