// FILE: app/src/main/java/com/sandeep/ganitabigyan/SettingsScreen.kt

package com.sandeep.ganitabigyan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data class to hold language information for the new UI
private data class LanguageInfo(
    val code: String,
    val nativeName: String,
    val englishName: String
)

//===================================================================
// 1. SETTINGS SCREEN (Corrected)
//===================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val themePreference by settingsViewModel.themePreference.collectAsStateWithLifecycle()
    val isVibrationEnabled by settingsViewModel.isVibrationEnabled.collectAsStateWithLifecycle()
    val isSoundEnabled by settingsViewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val morningTime by settingsViewModel.morningReminderTime.collectAsStateWithLifecycle()
    val eveningTime by settingsViewModel.eveningReminderTime.collectAsStateWithLifecycle()
    val currentLanguageCode by settingsViewModel.language.collectAsStateWithLifecycle()
    val areRemindersEnabled by settingsViewModel.areRemindersEnabled.collectAsStateWithLifecycle()

    // *** FIX: Corrected mutableStateOf ***
    var showMorningTimePicker by remember { mutableStateOf(false) }
    var showEveningTimePicker by remember { mutableStateOf(false) }
    val timePickerStateMorning = rememberTimePickerState(initialHour = morningTime.split(":")[0].toInt(), initialMinute = morningTime.split(":")[1].toInt(), is24Hour = false)
    val timePickerStateEvening = rememberTimePickerState(initialHour = eveningTime.split(":")[0].toInt(), initialMinute = eveningTime.split(":")[1].toInt(), is24Hour = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back_button_desc)) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            SettingsCategory(title = stringResource(R.string.settings_category_appearance))
            // *** FIX: Corrected function call to getCurrentThemeName ***
            SettingsClickableItem(title = stringResource(id = R.string.settings_theme_title), description = getCurrentThemeName(theme = themePreference), icon = Icons.Default.Brightness4, onClick = { navController.navigate(AppDestinations.THEME_SELECTION_ROUTE) })
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategory(title = stringResource(R.string.settings_category_general))
            SettingsClickableItem(title = stringResource(id = R.string.settings_language_title), description = stringResource(id = getLanguageStringId(code = currentLanguageCode)), icon = Icons.Default.Language, onClick = { navController.navigate(AppDestinations.LANGUAGE_SELECTION_ROUTE) })
            SettingsSwitchItem(title = stringResource(R.string.settings_sound_title), description = stringResource(R.string.settings_sound_description), icon = Icons.Default.VolumeUp, checked = isSoundEnabled, onCheckedChange = { settingsViewModel.setSoundEnabled(it) })
            SettingsSwitchItem(title = stringResource(R.string.settings_vibration_title), description = stringResource(R.string.settings_vibration_description), icon = Icons.Default.Vibration, checked = isVibrationEnabled, onCheckedChange = { settingsViewModel.setVibrationEnabled(it) })
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategory(title = stringResource(R.string.settings_category_reminders))
            SettingsSwitchItem(title = stringResource(R.string.settings_reminders_enabled_title), description = stringResource(R.string.settings_reminders_enabled_description), icon = Icons.Default.NotificationsActive, checked = areRemindersEnabled, onCheckedChange = { settingsViewModel.onRemindersToggled(it) })
            AnimatedVisibility(visible = areRemindersEnabled) {
                Column {
                    SettingsClickableItem(title = stringResource(R.string.settings_morning_reminder_title), description = formatTime(morningTime, context), icon = Icons.Default.Notifications, onClick = { showMorningTimePicker = true })
                    SettingsClickableItem(title = stringResource(R.string.settings_evening_reminder_title), description = formatTime(eveningTime, context), icon = Icons.Default.Notifications, onClick = { showEveningTimePicker = true })
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            SettingsCategory(title = stringResource(R.string.settings_category_other))
            SettingsClickableItem(title = stringResource(R.string.settings_about_app_title), description = stringResource(R.string.settings_about_app_description), icon = Icons.Default.Info, onClick = { navController.navigate(AppDestinations.ABOUT_ROUTE) })
        }
    }

    if (showMorningTimePicker) {
        TimePickerDialog(onDismiss = { showMorningTimePicker = false }, onConfirm = { val newTime = String.format("%02d:%02d", timePickerStateMorning.hour, timePickerStateMorning.minute); settingsViewModel.setMorningReminderTime(newTime); showMorningTimePicker = false; Toast.makeText(context, context.getString(R.string.toast_morning_reminder_set), Toast.LENGTH_SHORT).show() }, state = timePickerStateMorning)
    }
    if (showEveningTimePicker) {
        TimePickerDialog(onDismiss = { showEveningTimePicker = false }, onConfirm = { val newTime = String.format("%02d:%02d", timePickerStateEvening.hour, timePickerStateEvening.minute); settingsViewModel.setEveningReminderTime(newTime); showEveningTimePicker = false; Toast.makeText(context, context.getString(R.string.toast_evening_reminder_set), Toast.LENGTH_SHORT).show() }, state = timePickerStateEvening)
    }
}

//===================================================================
// 2. LANGUAGE SELECTION SCREEN (Corrected)
//===================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = (LocalContext.current as? Activity)
    val currentLanguageCode by settingsViewModel.language.collectAsStateWithLifecycle()
    // *** FIX: Corrected mutableStateOf ***
    var showRestartDialog by remember { mutableStateOf(false) }
    var languageToRestart by remember { mutableStateOf<String?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val languages = remember {
        listOf(
            LanguageInfo("or", "ଓଡ଼ିଆ", "Odia"),
            LanguageInfo("en", "English", ""),
            LanguageInfo("system", "System Default", ""),
            LanguageInfo("sa", "संस्कृतम्", "Sanskrit"),
            LanguageInfo("hi", "हिन्दी", "Hindi"),
            LanguageInfo("te", "తెలుగు", "Telugu"),
            LanguageInfo("bn", "বাংলা", "Bengali"),
            LanguageInfo("gu", "ગુજરાતી", "Gujarati"),
            LanguageInfo("as", "অসমীয়া", "Assamese"),
            LanguageInfo("ml", "മലയാളം", "Malayalam"),
            LanguageInfo("ur", "اردو", "Urdu")
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_language_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back_button_desc))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(languages) { language ->
                LanguageRow(
                    language = language,
                    isSelected = currentLanguageCode == language.code,
                    onClick = {
                        if (currentLanguageCode != language.code) {
                            languageToRestart = language.code
                            showRestartDialog = true
                        }
                    }
                )
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }

    if (showRestartDialog) {
        languageToRestart?.let { newLangCode ->
            WithLocale(languageCode = newLangCode) {
                RestartBottomSheet(
                    currentLanguageName = getCurrentLanguageName(code = currentLanguageCode, isEnglishName = false),
                    newLanguageName = getCurrentLanguageName(code = newLangCode, isEnglishName = false),
                    onDismiss = { showRestartDialog = false },
                    onConfirm = {
                        showRestartDialog = false
                        scope.launch {
                            settingsViewModel.saveLanguage(newLangCode)
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            activity?.finish()
                        }
                    }
                )
            }
        }
    }
}

//===================================================================
// 3. THEME SELECTION SCREEN
//===================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentTheme by settingsViewModel.themePreference.collectAsStateWithLifecycle()
    val customColorHex by settingsViewModel.customThemeColor.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val customColors = remember {
        listOf(
            Color(0xFF6750A4), Color(0xFFE91E63), Color(0xFF009688), Color(0xFFF44336),
            Color(0xFF4CAF50), Color(0xFF03A9F4), Color(0xFFFF9800), Color(0xFF795548),
            Color(0xFF3F51B5), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFF8BC34A)
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_theme_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back_button_desc))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                ThemeItem(
                    title = stringResource(R.string.theme_option_system),
                    isSelected = currentTheme == AppTheme.SYSTEM,
                    onClick = { settingsViewModel.setThemePreference(AppTheme.SYSTEM) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.White, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                )
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            item {
                ThemeItem(
                    title = stringResource(R.string.theme_option_light),
                    isSelected = currentTheme == AppTheme.LIGHT,
                    onClick = { settingsViewModel.setThemePreference(AppTheme.LIGHT) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            item {
                ThemeItem(
                    title = stringResource(R.string.theme_option_dark),
                    isSelected = currentTheme == AppTheme.DARK,
                    onClick = { settingsViewModel.setThemePreference(AppTheme.DARK) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF202124))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            item {
                ThemeItem(
                    title = stringResource(R.string.theme_option_amoled),
                    isSelected = currentTheme == AppTheme.AMOLED,
                    onClick = { settingsViewModel.setThemePreference(AppTheme.AMOLED) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            item {
                ThemeItem(
                    title = stringResource(R.string.theme_option_custom),
                    isSelected = currentTheme == AppTheme.CUSTOM,
                    onClick = { settingsViewModel.setThemePreference(AppTheme.CUSTOM) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(customColorHex)))
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }

            // The animated color grid
            item {
                AnimatedVisibility(
                    visible = currentTheme == AppTheme.CUSTOM,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 56.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.heightIn(max = 200.dp) // prevent it from being too tall
                    ) {
                        items(customColors) { color ->
                            val colorHex = String.format("#%06X", (0xFFFFFF and color.toArgb()))
                            val isSelected = colorHex == customColorHex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                                    .clickable { settingsViewModel.setCustomThemeColor(colorHex) }
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

//===================================================================
// 4. HELPER COMPOSABLES
//===================================================================

@Composable
private fun ThemeItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    preview: @Composable () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title) },
        leadingContent = { preview() },
        trailingContent = {
            RadioButton(selected = isSelected, onClick = null)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun WithLocale(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val locale = if (languageCode == "system") {
        ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0] ?: Locale.getDefault()
    } else {
        Locale(languageCode)
    }

    val context = LocalContext.current
    val localizedContext = remember(context, locale) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestartBottomSheet(
    currentLanguageName: String,
    newLanguageName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.language_restart_dialog_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.language_restart_dynamic_message, currentLanguageName, newLanguageName), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(R.string.dialog_restart))
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    }
}

@Composable
private fun LanguageRow(language: LanguageInfo, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = language.nativeName, style = MaterialTheme.typography.bodyLarge)
            Text(text = language.englishName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        RadioButton(selected = isSelected, onClick = null)
    }
}

@Composable
private fun getCurrentThemeName(theme: String): String {
    return when (theme) { AppTheme.LIGHT -> stringResource(R.string.theme_option_light); AppTheme.DARK -> stringResource(R.string.theme_option_dark); AppTheme.AMOLED -> stringResource(R.string.theme_option_amoled); AppTheme.CUSTOM -> stringResource(R.string.theme_option_custom); else -> stringResource(R.string.theme_option_system) }
}

@Composable
private fun getCurrentLanguageName(code: String, isEnglishName: Boolean = false): String {
    return if (isEnglishName) {
        when (code) {
            "system" -> "System Default"; "en" -> "English"; "or" -> "Odia"; "sa" -> "Sanskrit"; "hi" -> "Hindi"; "te" -> "Telugu"; "bn" -> "Bengali"; "gu" -> "Gujarati"; "as" -> "Assamese"; "ml" -> "Malayalam"; "ur" -> "Urdu"; else -> "System Default"
        }
    } else {
        when (code) {
            "system" -> stringResource(R.string.language_system_default); "en" -> "English"; "or" -> "ଓଡ଼ିଆ"; "sa" -> "संस्कृतम्"; "hi" -> "हिन्दी"; "te" -> "తెలుగు"; "bn" -> "বাংলা"; "gu" -> "ગુજરાતી"; "as" -> "অসমীয়া"; "ml" -> "മലയാളം"; "ur" -> "اردو"; else -> stringResource(R.string.language_system_default)
        }
    }
}

private fun formatTime(time24h: String, context: Context): String {
    return try { val sdf24h = SimpleDateFormat("HH:mm", Locale.US); val sdf12h = SimpleDateFormat("hh:mm a", Locale.US); val date = sdf24h.parse(time24h); val formattedTime = sdf12h.format(date!!); formattedTime.toLocaleNumerals(context) } catch (e: Exception) { time24h }
}
@Composable
private fun SettingsCategory(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}
@Composable
private fun SettingsSwitchItem(title: String, description: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = title, style = MaterialTheme.typography.bodyLarge); Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(checked = checked, onCheckedChange = onCheckedChange) }
}
@Composable
private fun SettingsClickableItem(title: String, description: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(16.dp)); Column(modifier = Modifier.weight(1f)) { Text(text = title, style = MaterialTheme.typography.bodyLarge); Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(onDismiss: () -> Unit, onConfirm: () -> Unit, state: TimePickerState) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.dialog_ok)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } }, text = { TimePicker(state = state) })
}
private fun getLanguageStringId(code: String): Int {
    return when (code) {
        "or" -> R.string.language_odia
        "en" -> R.string.language_english
        "sa" -> R.string.language_sanskrit
        "hi" -> R.string.language_hindi
        "te" -> R.string.language_telugu
        "bn" -> R.string.language_bengali
        "gu" -> R.string.language_gujarati
        "as" -> R.string.language_assamese
        "ml" -> R.string.language_malayalam
        "ur" -> R.string.language_urdu
        else -> R.string.language_system_default
    }
}