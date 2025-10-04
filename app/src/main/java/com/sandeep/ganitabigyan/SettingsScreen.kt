// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsScreen.kt

package com.sandeep.ganitabigyan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = (LocalContext.current as? Activity)

    // Collect all states from ViewModel
    val themePreference by settingsViewModel.themePreference.collectAsStateWithLifecycle()
    val isVibrationEnabled by settingsViewModel.isVibrationEnabled.collectAsStateWithLifecycle()
    val isSoundEnabled by settingsViewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val morningTime by settingsViewModel.morningReminderTime.collectAsStateWithLifecycle()
    val eveningTime by settingsViewModel.eveningReminderTime.collectAsStateWithLifecycle()
    val currentLanguageCode by settingsViewModel.language.collectAsStateWithLifecycle()
    val areRemindersEnabled by settingsViewModel.areRemindersEnabled.collectAsStateWithLifecycle()

    // State for managing dialog visibility
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var languageToRestart by remember { mutableStateOf<String?>(null) }
    var showMorningTimePicker by remember { mutableStateOf(false) }
    var showEveningTimePicker by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorPickerDialog by remember { mutableStateOf(false) }

    // State for Time Pickers
    val timePickerStateMorning = rememberTimePickerState(
        initialHour = morningTime.split(":")[0].toInt(),
        initialMinute = morningTime.split(":")[1].toInt(),
        is24Hour = false
    )
    val timePickerStateEvening = rememberTimePickerState(
        initialHour = eveningTime.split(":")[0].toInt(),
        initialMinute = eveningTime.split(":")[1].toInt(),
        is24Hour = false
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back_button_desc))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsCategory(title = stringResource(R.string.settings_category_appearance))
            SettingsClickableItem(
                title = stringResource(id = R.string.settings_theme_title),
                description = getCurrentThemeName(theme = themePreference),
                icon = Icons.Default.Brightness4,
                onClick = { showThemeDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategory(title = stringResource(R.string.settings_category_general))
            SettingsClickableItem(
                title = stringResource(id = R.string.settings_language_title),
                description = getCurrentLanguageName(code = currentLanguageCode),
                icon = Icons.Default.Language,
                onClick = { showLanguageDialog = true }
            )
            SettingsSwitchItem(
                title = stringResource(R.string.settings_sound_title),
                description = stringResource(R.string.settings_sound_description),
                icon = Icons.Default.VolumeUp,
                checked = isSoundEnabled,
                onCheckedChange = { settingsViewModel.setSoundEnabled(it) }
            )
            SettingsSwitchItem(
                title = stringResource(R.string.settings_vibration_title),
                description = stringResource(R.string.settings_vibration_description),
                icon = Icons.Default.Vibration,
                checked = isVibrationEnabled,
                onCheckedChange = { settingsViewModel.setVibrationEnabled(it) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategory(title = stringResource(R.string.settings_category_reminders))
            SettingsSwitchItem(
                title = stringResource(R.string.settings_reminders_enabled_title),
                description = stringResource(R.string.settings_reminders_enabled_description),
                icon = Icons.Default.NotificationsActive,
                checked = areRemindersEnabled,
                onCheckedChange = { settingsViewModel.onRemindersToggled(it) }
            )
            AnimatedVisibility(visible = areRemindersEnabled) {
                Column {
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_morning_reminder_title),
                        description = formatTime(morningTime, context),
                        icon = Icons.Default.Notifications,
                        onClick = { showMorningTimePicker = true }
                    )
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_evening_reminder_title),
                        description = formatTime(eveningTime, context),
                        icon = Icons.Default.Notifications,
                        onClick = { showEveningTimePicker = true }
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategory(title = stringResource(R.string.settings_category_other))
            SettingsClickableItem(
                title = stringResource(R.string.settings_about_app_title),
                description = stringResource(R.string.settings_about_app_description),
                icon = Icons.Default.Info,
                onClick = { navController.navigate(AppDestinations.ABOUT_ROUTE) }
            )
        }
    }

    // --- DIALOGS ---
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themePreference,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                settingsViewModel.setThemePreference(theme)
                showThemeDialog = false
                if (theme == AppTheme.CUSTOM) {
                    showColorPickerDialog = true
                }
            }
        )
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            onDismiss = { showColorPickerDialog = false },
            onColorSelected = { color ->
                val hexColor = String.format("#%06X", (0xFFFFFF and color.toArgb()))
                settingsViewModel.setCustomThemeColor(hexColor)
                showColorPickerDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLanguageCode,
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { code ->
                showLanguageDialog = false
                languageToRestart = code
                showRestartDialog = true
            }
        )
    }

    if (showRestartDialog) {
        RestartConfirmationDialog(
            onDismiss = { showRestartDialog = false },
            onConfirm = {
                showRestartDialog = false
                languageToRestart?.let { langCode ->
                    scope.launch {
                        settingsViewModel.saveLanguage(langCode)
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        activity?.finish()
                    }
                }
            }
        )
    }

    if (showMorningTimePicker) {
        TimePickerDialog(
            onDismiss = { showMorningTimePicker = false },
            onConfirm = {
                val newTime = String.format("%02d:%02d", timePickerStateMorning.hour, timePickerStateMorning.minute)
                settingsViewModel.setMorningReminderTime(newTime)
                showMorningTimePicker = false
                Toast.makeText(context, context.getString(R.string.toast_morning_reminder_set), Toast.LENGTH_SHORT).show()
            },
            state = timePickerStateMorning
        )
    }

    if (showEveningTimePicker) {
        TimePickerDialog(
            onDismiss = { showEveningTimePicker = false },
            onConfirm = {
                val newTime = String.format("%02d:%02d", timePickerStateEvening.hour, timePickerStateEvening.minute)
                settingsViewModel.setEveningReminderTime(newTime)
                showEveningTimePicker = false
                Toast.makeText(context, context.getString(R.string.toast_evening_reminder_set), Toast.LENGTH_SHORT).show()
            },
            state = timePickerStateEvening
        )
    }
}

@Composable
private fun ThemeSelectionDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onThemeSelected: (String) -> Unit
) {
    val themes = remember {
        listOf(
            AppTheme.SYSTEM to R.string.theme_option_system,
            AppTheme.LIGHT to R.string.theme_option_light,
            AppTheme.DARK to R.string.theme_option_dark,
            AppTheme.AMOLED to R.string.theme_option_amoled,
            AppTheme.CUSTOM to R.string.theme_option_custom,
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.theme_dialog_title)) },
        text = {
            Column {
                themes.forEach { (theme, stringId) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentTheme == theme, onClick = { onThemeSelected(theme) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = stringId))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }
    )
}

@Composable
private fun ColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val colors = remember {
        listOf(
            Color(0xFF6750A4), // Material Purple
            Color(0xFFE91E63), // Pink
            Color(0xFF4CAF50), // Green
            Color(0xFF03A9F4), // Light Blue
            Color(0xFFFF9800), // Orange
            Color(0xFF009688), // Teal
            Color(0xFF795548), // Brown
            Color(0xFFF44336)  // Red
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.custom_color_dialog_title)) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(top = 16.dp)
            ) {
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        }
    )
}


@Composable
private fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = remember {
        listOf(
            "system" to R.string.language_system_default,
            "or" to R.string.language_odia,
            "en" to R.string.language_english,
            "sa" to R.string.language_sanskrit,
            "hi" to R.string.language_hindi,
            "te" to R.string.language_telugu,
            "bn" to R.string.language_bengali,
            "gu" to R.string.language_gujarati,
            "as" to R.string.language_assamese,
            "ml" to R.string.language_malayalam
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.language_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.language_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(languages) { (code, stringId) ->
                        val isSelected = currentLanguageCode == code
                        val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        Text(
                            text = stringResource(id = stringId),
                            color = color,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (code != currentLanguageCode) onLanguageSelected(code) else onDismiss()
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

@Composable
private fun RestartConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_restart_dialog_title)) },
        text = { Text(stringResource(R.string.language_restart_dialog_message)) },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
        confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.dialog_restart)) } }
    )
}

@Composable
private fun getCurrentThemeName(theme: String): String {
    return when (theme) {
        AppTheme.LIGHT -> stringResource(R.string.theme_option_light)
        AppTheme.DARK -> stringResource(R.string.theme_option_dark)
        AppTheme.AMOLED -> stringResource(R.string.theme_option_amoled)
        AppTheme.CUSTOM -> stringResource(R.string.theme_option_custom)
        else -> stringResource(R.string.theme_option_system)
    }
}

@Composable
private fun getCurrentLanguageName(code: String): String {
    return when (code) {
        "system" -> stringResource(R.string.language_system_default)
        "en" -> stringResource(R.string.language_english)
        "or" -> stringResource(R.string.language_odia)
        "sa" -> stringResource(R.string.language_sanskrit)
        "hi" -> stringResource(R.string.language_hindi)
        "te" -> stringResource(R.string.language_telugu)
        "bn" -> stringResource(R.string.language_bengali)
        "gu" -> stringResource(R.string.language_gujarati)
        "as" -> stringResource(R.string.language_assamese)
        "ml" -> stringResource(R.string.language_malayalam)
        else -> stringResource(R.string.language_system_default)
    }
}

private fun formatTime(time24h: String, context: Context): String {
    return try {
        val sdf24h = SimpleDateFormat("HH:mm", Locale.US)
        val sdf12h = SimpleDateFormat("hh:mm a", Locale.US)
        val date = sdf24h.parse(time24h)
        val formattedTime = sdf12h.format(date!!)
        formattedTime.toLocaleNumerals(context)
    } catch (e: Exception) {
        time24h
    }
}

@Composable
private fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    state: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.dialog_ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
        text = { TimePicker(state = state) }
    )
}