// FILE: app/src/main/java/com/sandeep/ganitabigyan/WelcomeScreen.kt
// VERSION: FINAL - Fixes the app name display bug.

package com.sandeep.ganitabigyan

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private data class LanguageOption(val code: String, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var languageToRestart by remember { mutableStateOf<LanguageOption?>(null) }

    val languageOptions = remember {
        listOf(
            "or" to R.string.language_odia,
            "en" to R.string.language_english,
            "sa" to R.string.language_sanskrit,
            "hi" to R.string.language_hindi,
            "te" to R.string.language_telugu,
            "bn" to R.string.language_bengali,
            "gu" to R.string.language_gujarati,
            "as" to R.string.language_assamese,
            "ml" to R.string.language_malayalam
        ).map { (code, nameResId) ->
            // <<< THIS IS THE ONLY CHANGE NEEDED TO FIX THE BUG >>>
            // Create a completely new Configuration object for each language.
            val config = Configuration()
            config.setLocale(Locale(code))
            val localizedContext = context.createConfigurationContext(config)
            LanguageOption(code, localizedContext.getString(nameResId))
        }
    }

    val (selectedLanguage, setSelectedLanguage) = remember {
        val deviceLangCode = Locale.getDefault().language
        val initialLang = languageOptions.find { it.code == deviceLangCode } ?: languageOptions.first()
        mutableStateOf(initialLang)
    }

    var welcomeText by remember { mutableStateOf("") }
    var chooseLanguageText by remember { mutableStateOf("") }
    var continueText by remember { mutableStateOf("") }

    LaunchedEffect(selectedLanguage) {
        val locale = Locale(selectedLanguage.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        welcomeText = localizedContext.getString(R.string.welcome_title)
        chooseLanguageText = localizedContext.getString(R.string.choose_language_prompt)
        continueText = localizedContext.getString(R.string.button_continue)
    }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var currentAppNameIndex by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(2000)
            currentAppNameIndex = (currentAppNameIndex + 1) % languageOptions.size
        }
    }

    fun changeLanguageAndRestart(language: LanguageOption) {
        if (isLoading) return
        scope.launch {
            isLoading = true
            settingsViewModel.saveLanguage(language.code)
            settingsViewModel.setWelcomeCompleted()
            val activity = (context as? Activity)
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            activity?.finish()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Image(painter = painterResource(id = R.drawable.ic_welcome_mascot), contentDescription = "Welcome Mascot", modifier = Modifier.fillMaxWidth(0.7f), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(targetState = currentAppNameIndex, transitionSpec = { fadeIn(initialAlpha = 0.3f) togetherWith fadeOut(targetAlpha = 0.3f) }, label = "AppNameAnimation") { index ->
                val animLangCode = languageOptions[index].code
                val animConfig = Configuration(context.resources.configuration)
                animConfig.setLocale(Locale(animLangCode))
                val animLocalizedContext = context.createConfigurationContext(animConfig)
                Text(text = animLocalizedContext.getString(R.string.app_name), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            Text(text = welcomeText, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))

            ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { expanded -> isDropdownExpanded = expanded }) {
                OutlinedTextField(value = selectedLanguage.name, onValueChange = {}, readOnly = true, label = { Text(chooseLanguageText) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                    languageOptions.forEach { languageOption ->
                        DropdownMenuItem(
                            text = { Text(languageOption.name) },
                            onClick = {
                                setSelectedLanguage(languageOption)
                                isDropdownExpanded = false
                                languageToRestart = languageOption
                                showRestartDialog = true
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    languageToRestart = selectedLanguage
                    showRestartDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                AnimatedContent(targetState = isLoading, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "button content animation") { loadingState ->
                    if (loadingState) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Text(text = continueText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showRestartDialog) {
        RestartConfirmationDialog(
            onDismiss = { showRestartDialog = false },
            onConfirm = {
                showRestartDialog = false
                languageToRestart?.let { lang ->
                    changeLanguageAndRestart(lang)
                }
            }
        )
    }
}

@Composable
private fun RestartConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_restart_dialog_title)) },
        text = { Text(stringResource(R.string.language_restart_dialog_message)) },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_restart))
            }
        }
    )
}