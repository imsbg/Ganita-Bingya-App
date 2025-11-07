// FILE: app/src/main/java/com/sandeep/ganitabigyan/WelcomeScreen.kt

package com.sandeep.ganitabigyan

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.Locale

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

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFFEAF2F8)
    val cardColor = Color.White
    val primaryColor = Color(0xFF3498DB)
    val buttonColor = Color(0xFF1A5276)
    val textColor = Color(0xFF2C3E50)

    val allLanguageOptions = remember {
        // <<< THIS LIST IS NOW MANUALLY SORTED for predictable, correct alphabetical order >>>
        listOf(
            "or" to R.string.language_odia,       // ଓଡ଼ିଆ
            "sa" to R.string.language_sanskrit,   // संस्कृतम्
            "en" to R.string.language_english,
            "hi" to R.string.language_hindi,      // हिन्दी
            "as" to R.string.language_assamese,   // ଅସମୀୟା
            "gu" to R.string.language_gujarati,   // ગુજરાતી
            "te" to R.string.language_telugu,     // తెలుగు
            "bn" to R.string.language_bengali,    // বাংলা
            "ml" to R.string.language_malayalam,  // മലയാളം
            "ur" to R.string.language_urdu       // اردو

        ).map { (code, nameResId) ->
            val config = Configuration()
            config.setLocale(Locale(code))
            val localizedContext = context.createConfigurationContext(config)
            LanguageOption(code, localizedContext.getString(nameResId))
        }
    }

    val (selectedLanguage, setSelectedLanguage) = remember {
        mutableStateOf(allLanguageOptions.find { it.code == "en" } ?: allLanguageOptions.first())
    }

    val recommendedLanguages = remember(allLanguageOptions) {
        val odia = allLanguageOptions.find { it.code == "or" }
        val english = allLanguageOptions.find { it.code == "en" }
        listOfNotNull(odia, english)
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use the new "Choose a language" string for the header
                Text(
                    text = localizedString(selectedLanguage.code, R.string.choose_a_language),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                item {
                    Text(
                        text = localizedString(selectedLanguage.code, R.string.recommended_language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(recommendedLanguages) { language ->
                    LanguageRow(
                        language = language,
                        isSelected = selectedLanguage.code == language.code,
                        onSelect = { setSelectedLanguage(language) },
                        colors = LanguageRowColors(
                            card = cardColor,
                            highlightedCard = primaryColor,
                            text = textColor,
                            highlightedText = Color.White,
                            iconBg = backgroundColor,
                            highlightedIconBg = Color.White,
                            iconTint = primaryColor,
                            highlightedIconTint = primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Text(
                        text = localizedString(selectedLanguage.code, R.string.all_languages),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                // The list is now taken directly from the manually sorted 'allLanguageOptions'
                items(allLanguageOptions) { language ->
                    LanguageRow(
                        language = language,
                        isSelected = selectedLanguage.code == language.code,
                        onSelect = { setSelectedLanguage(language) },
                        colors = LanguageRowColors(
                            card = cardColor,
                            highlightedCard = primaryColor,
                            text = textColor,
                            highlightedText = Color.White,
                            iconBg = backgroundColor,
                            highlightedIconBg = Color.White,
                            iconTint = primaryColor,
                            highlightedIconTint = primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Button(
                onClick = { changeLanguageAndRestart(selectedLanguage) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                enabled = !isLoading
            ) {
                // Use the new "Next" string for the button
                Text(
                    text = localizedString(selectedLanguage.code, R.string.button_next),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

private data class LanguageRowColors(
    val card: Color,
    val text: Color,
    val iconBg: Color,
    val iconTint: Color,
    val highlightedCard: Color,
    val highlightedText: Color,
    val highlightedIconBg: Color,
    val highlightedIconTint: Color,
)

@Composable
private fun LanguageRow(
    language: LanguageOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
    colors: LanguageRowColors
) {
    val cardColor = if (isSelected) colors.highlightedCard else colors.card
    val textColor = if (isSelected) colors.highlightedText else colors.text
    val iconBgColor = if (isSelected) colors.highlightedIconBg else colors.iconBg
    val iconTintColor = if (isSelected) colors.highlightedIconTint else colors.iconTint

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                val firstLetter = language.name.take(1)
                Text(
                    text = firstLetter,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTintColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = language.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isSelected) Color.White else colors.highlightedCard,
                    unselectedColor = textColor.copy(alpha = 0.6f)
                )
            )
        }
    }
}