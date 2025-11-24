// FILE: app/src/main/java/com/sandeep/ganitabigyan/WelcomeScreen.kt

package com.sandeep.ganitabigyan

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.Locale

// --- Data Classes and Helper Functions (Largely Unchanged) ---

private data class LanguageOption(val code: String, val name: String)

@Composable
private fun localizedString(localeCode: String, stringResId: Int): String {
    val context = LocalContext.current
    return remember(localeCode) {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(localeCode))
        val localizedContext = context.createConfigurationContext(config)
        localizedContext.getString(stringResId)
    }
}

// --- New Theming Object for a Cohesive Look ---

private object WelcomeScreenColors {
    val background = Color(0xFFF8F9FC)
    val textPrimary = Color(0xFF1D232E)
    val textSecondary = Color(0xFF6E7B8B)
    val accent = Color(0xFF4A80F0)
    val accentContent = Color.White
    val cardBackground = Color.White
    val cardSelectedBackground = Color(0xFFEAF2FF)
    val cardBorder = Color(0xFFE4E9F1)
}

// --- Main Welcome Screen Composable (Redesigned) ---

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit, // Note: This parameter is kept but the internal logic uses a restart flow.
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // --- Language Data Logic (Unchanged) ---
    val allLanguageOptions = remember {
        listOf(
            "or" to R.string.language_odia, "sa" to R.string.language_sanskrit,
            "en" to R.string.language_english, "hi" to R.string.language_hindi,
            "as" to R.string.language_assamese, "gu" to R.string.language_gujarati,
            "te" to R.string.language_telugu, "bn" to R.string.language_bengali,
            "ml" to R.string.language_malayalam, "ur" to R.string.language_urdu
        ).map { (code, nameResId) ->
            val config = Configuration()
            config.setLocale(Locale(code))
            val localizedContext = context.createConfigurationContext(config)
            LanguageOption(code, localizedContext.getString(nameResId))
        }
    }

    val (selectedLanguage, setSelectedLanguage) = remember {
        mutableStateOf(allLanguageOptions.find { it.code == "or" } ?: allLanguageOptions.first())
    }

    val recommendedLanguages = remember(allLanguageOptions) {
        val odia = allLanguageOptions.find { it.code == "or" }
        val english = allLanguageOptions.find { it.code == "en" }
        listOfNotNull(odia, english)
    }

    // --- App Restart Logic (Unchanged) ---
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

    // --- UI Structure ---
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WelcomeScreenColors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            ScreenHeader(selectedLanguageCode = selectedLanguage.code)
            LanguageList(
                modifier = Modifier.weight(1f),
                selectedLanguage = selectedLanguage,
                recommendedLanguages = recommendedLanguages,
                allLanguages = allLanguageOptions,
                onLanguageSelected = { setSelectedLanguage(it) }
            )
            ContinueButton(
                isLoading = isLoading,
                selectedLanguageCode = selectedLanguage.code,
                onClick = { changeLanguageAndRestart(selectedLanguage) }
            )
        }
    }
}

// --- Reusable UI Components for a Cleaner Structure ---

@Composable
private fun ScreenHeader(selectedLanguageCode: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Translate,
            contentDescription = null,
            tint = WelcomeScreenColors.accent,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = localizedString(selectedLanguageCode, R.string.choose_a_language),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = WelcomeScreenColors.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = localizedString(selectedLanguageCode, R.string.welcome_subtitle), // Assumes new string R.string.welcome_subtitle exists
            style = MaterialTheme.typography.bodyMedium,
            color = WelcomeScreenColors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LanguageList(
    modifier: Modifier = Modifier,
    selectedLanguage: LanguageOption,
    recommendedLanguages: List<LanguageOption>,
    allLanguages: List<LanguageOption>,
    onLanguageSelected: (LanguageOption) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(localizedString(selectedLanguage.code, R.string.recommended_language))
        }
        items(recommendedLanguages) { language ->
            LanguageRow(
                language = language,
                isSelected = selectedLanguage.code == language.code,
                onSelect = { onLanguageSelected(language) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle(localizedString(selectedLanguage.code, R.string.all_languages))
        }
        items(allLanguages) { language ->
            LanguageRow(
                language = language,
                isSelected = selectedLanguage.code == language.code,
                onSelect = { onLanguageSelected(language) }
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = WelcomeScreenColors.textSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun ContinueButton(
    isLoading: Boolean,
    selectedLanguageCode: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenColors.accent),
        enabled = !isLoading
    ) {
        Text(
            text = localizedString(selectedLanguageCode, R.string.button_next), // Using "button_next" from original
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun LanguageRow(
    language: LanguageOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val animSpec = tween<Color>(durationMillis = 300)
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) WelcomeScreenColors.cardSelectedBackground else WelcomeScreenColors.cardBackground,
        animationSpec = animSpec
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) WelcomeScreenColors.accent else WelcomeScreenColors.textPrimary,
        animationSpec = animSpec
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) WelcomeScreenColors.accent else WelcomeScreenColors.cardBorder,
        animationSpec = animSpec
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageInitial(language = language, isSelected = isSelected)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = language.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = WelcomeScreenColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageInitial(language: LanguageOption, isSelected: Boolean) {
    val animSpec = tween<Color>(durationMillis = 300)
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) WelcomeScreenColors.accent else WelcomeScreenColors.cardSelectedBackground,
        animationSpec = animSpec
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) WelcomeScreenColors.accentContent else WelcomeScreenColors.accent,
        animationSpec = animSpec
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = language.name.take(1),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}