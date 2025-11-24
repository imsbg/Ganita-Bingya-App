// FILE: app/src/main/java/com/sandeep/ganitabigyan/NumberToTextScreen.kt

package com.sandeep.ganitabigyan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberToTextScreen(
    onNavigateBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    // Results
    var selectedLanguageResult by remember { mutableStateOf("") }
    var englishResult by remember { mutableStateOf("") }

    // UI States
    var showOtherScripts by remember { mutableStateOf(false) }
    var isScreenshotMode by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val currentLocale = context.resources.configuration.locales[0]
    val currentLangCode = currentLocale.language
    val currentLangName = currentLocale.displayLanguage

    // Helper to handle copying
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Number Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.msg_copied), Toast.LENGTH_SHORT).show()
    }

    // Helper to enter screenshot mode safely
    fun enterScreenshotMode() {
        focusManager.clearFocus()
        isScreenshotMode = true
    }

    Scaffold(
        topBar = {
            if (!isScreenshotMode) {
                TopAppBar(
                    title = { Text(stringResource(R.string.menu_number_to_text)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button_description)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { enterScreenshotMode() }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Screenshot Mode")
                        }
                    }
                )
            }
        }
    ) { padding ->
        // Handle click to exit screenshot mode
        val modifier = if (isScreenshotMode) {
            Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isScreenshotMode = false }
        } else {
            Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() }
        }

        Column(
            modifier = modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // --- INPUT SECTION ---
            if (!isScreenshotMode) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            if (newValue.length <= 10) {
                                inputText = newValue
                                if (inputText.isNotEmpty()) {
                                    val number = inputText.toLongOrNull() ?: 0L
                                    selectedLanguageResult = convertWithLocalizedResources(context, number)
                                    englishResult = convertToEnglish(number)
                                } else {
                                    selectedLanguageResult = ""
                                    englishResult = ""
                                }
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.ntt_enter_number)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Text(
                    text = stringResource(R.string.msg_exit_screenshot),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )
            }

            if (selectedLanguageResult.isNotEmpty()) {

                // --- CARD 1: SELECTED LANGUAGE ---
                ResultCard(
                    label = currentLangName,
                    nativeNum = convertToNativeDigits(inputText, currentLangCode),
                    text = selectedLanguageResult,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onCopy = if (!isScreenshotMode) { { copyToClipboard(selectedLanguageResult) } } else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // --- CARD 2: ENGLISH ---
                if (currentLangCode != "en") {
                    ResultCard(
                        label = "English",
                        nativeNum = inputText,
                        text = englishResult,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onCopy = if (!isScreenshotMode) { { copyToClipboard(englishResult) } } else null
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- CHEQUE / BANK FORMAT ---
                ChequeFormatCard(
                    number = inputText.toLongOrNull() ?: 0L,
                    localText = selectedLanguageResult,
                    englishText = englishResult,
                    currentLangCode = currentLangCode
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- SHOW NATIVE DIGITS BUTTON ---
                if (!isScreenshotMode) {
                    Button(
                        onClick = { showOtherScripts = !showOtherScripts },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showOtherScripts) "Hide local language numerals" else "Show local language numerals")
                    }

                    AnimatedVisibility(visible = showOtherScripts) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            val languages = listOf(
                                Triple("or", "ଓଡ଼ିଆ", "or"),
                                Triple("hi", "संस्कृतम् / हिन्दी", "hi"),
                                Triple("te", "తెలుగు", "te"),
                                Triple("bn", "অসমীয়া / বাংলা", "bn"),
                                Triple("gu", "ગુજરાતી", "gu"),
                                Triple("ur", "اُردُو", "ur")
                            )

                            Text(
                                text = "Native Digits:",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                            )

                            languages.forEach { (code, name, digitCode) ->
                                if (code != currentLangCode) {
                                    LanguageRowItem(
                                        name = name,
                                        nativeNum = convertToNativeDigits(inputText, digitCode)
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENT: CHEQUE CARD ---
@Composable
fun ChequeFormatCard(
    number: Long,
    localText: String,
    englishText: String,
    currentLangCode: String
) {
    var showInEnglish by remember { mutableStateOf(false) }
    val isAppEnglish = currentLangCode == "en"
    val displayEnglish = showInEnglish || isAppEnglish

    val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    val formattedNumber = numberFormat.format(number)

    val displayAmount = if (displayEnglish) formattedNumber else convertToNativeDigits(formattedNumber, currentLangCode)
    val displayText = if (displayEnglish) englishText else localText

    val label = stringResource(R.string.lbl_bank_format)
    val symbol = "₹"
    val suffix = "/-"
    val wordRupees = if (displayEnglish) "Rupees" else stringResource(R.string.word_rupees)
    val wordOnly = if (displayEnglish) "Only" else stringResource(R.string.word_only)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF006064),
                    fontWeight = FontWeight.Bold
                )

                if (!isAppEnglish) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (showInEnglish) "Eng" else "Local",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF006064)
                        )
                        Switch(
                            checked = showInEnglish,
                            onCheckedChange = { showInEnglish = it },
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$symbol $displayAmount $suffix",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$displayText $wordRupees $wordOnly",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF006064),
                lineHeight = 24.sp
            )
        }
    }
}

// --- RESULT CARD ---
@Composable
fun ResultCard(
    label: String,
    nativeNum: String,
    text: String,
    color: Color,
    textColor: Color,
    onCopy: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = nativeNum,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.8f)
                    )

                    if (onCopy != null) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(32.dp).padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = textColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- FIXED LOGIC FUNCTIONS (No Special Cases) ---
private fun convertWithLocalizedResources(context: Context, n: Long): String {
    // 1. Fetch standard words array (0-100)
    val smallNumbers = context.resources.getStringArray(R.array.odia_number_words)

    // 2. Fetch suffixes
    val hundredSuffix = context.getString(R.string.word_hundreds_suffix) // e.g., "ଶହ"
    val thousandWord = context.getString(R.string.word_thousand) // e.g., "ହଜାର"
    val lakhWord = context.getString(R.string.word_lakh) // e.g., "ଲକ୍ଷ"
    // Fallback for Crore if not in strings.xml yet
    val croreWord = try { context.getString(R.string.word_crore) } catch(e:Exception) { "Crore" }

    // 0 to 100: Direct Lookup
    if (n in 0..100) {
        return smallNumbers[n.toInt()]
    }

    // Recursive Logic: Hundreds
    if (n < 1000) {
        val hundredPart = n / 100
        val remainder = n % 100
        val prefix = smallNumbers[hundredPart.toInt()]
        val rest = if (remainder != 0L) " " + convertWithLocalizedResources(context, remainder) else ""
        return "$prefix $hundredSuffix$rest"
    }
    // Recursive Logic: Thousands
    if (n < 100000) {
        val thousandPart = n / 1000
        val remainder = n % 1000
        val prefix = convertWithLocalizedResources(context, thousandPart)
        val rest = if (remainder != 0L) " " + convertWithLocalizedResources(context, remainder) else ""
        return "$prefix $thousandWord$rest"
    }
    // Recursive Logic: Lakhs
    if (n < 10000000) {
        val lakhPart = n / 100000
        val remainder = n % 100000
        val prefix = convertWithLocalizedResources(context, lakhPart)
        val rest = if (remainder != 0L) " " + convertWithLocalizedResources(context, remainder) else ""
        return "$prefix $lakhWord$rest"
    }
    // Recursive Logic: Crores
    val crorePart = n / 10000000
    val remainder = n % 10000000
    val prefix = convertWithLocalizedResources(context, crorePart)
    val rest = if (remainder != 0L) " " + convertWithLocalizedResources(context, remainder) else ""
    return "$prefix $croreWord$rest"
}

private fun convertToEnglish(n: Long): String {
    if (n == 0L) return "Zero"
    val units = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")
    if (n < 0) return "Minus " + convertToEnglish(-n)
    if (n < 20) return units[n.toInt()]
    if (n < 100) return tens[n.toInt() / 10] + (if (n % 10 != 0L) " " + units[n.toInt() % 10] else "")
    if (n < 1000) return units[n.toInt() / 100] + " Hundred" + (if (n % 100 != 0L) " " + convertToEnglish(n % 100) else "")
    if (n < 100000) return convertToEnglish(n / 1000) + " Thousand" + (if (n % 1000 != 0L) " " + convertToEnglish(n % 1000) else "")
    if (n < 10000000) return convertToEnglish(n / 100000) + " Lakh" + (if (n % 100000 != 0L) " " + convertToEnglish(n % 100000) else "")
    return convertToEnglish(n / 10000000) + " Crore" + (if (n % 10000000 != 0L) " " + convertToEnglish(n % 10000000) else "")
}

fun convertToNativeDigits(input: String, langCode: String): String {
    return input.map { char ->
        when (langCode) {
            "or" -> when (char) { '0' -> '୦'; '1' -> '୧'; '2' -> '୨'; '3' -> '୩'; '4' -> '୪'; '5' -> '୫'; '6' -> '୬'; '7' -> '୭'; '8' -> '୮'; '9' -> '୯'; else -> char }
            "hi", "sa", "mr" -> when (char) { '0' -> '०'; '1' -> '१'; '2' -> '२'; '3' -> '३'; '4' -> '४'; '5' -> '५'; '6' -> '६'; '7' -> '७'; '8' -> '८'; '9' -> '९'; else -> char }
            "bn", "as" -> when (char) { '0' -> '০'; '1' -> '১'; '2' -> '২'; '3' -> '৩'; '4' -> '৪'; '5' -> '৫'; '6' -> '৬'; '7' -> '৭'; '8' -> '৮'; '9' -> '৯'; else -> char }
            "gu" -> when (char) { '0' -> '૦'; '1' -> '૧'; '2' -> '૨'; '3' -> '૩'; '4' -> '૪'; '5' -> '૫'; '6' -> '૬'; '7' -> '૭'; '8' -> '૮'; '9' -> '૯'; else -> char }
            "te" -> when (char) { '0' -> '౦'; '1' -> '౧'; '2' -> '౨'; '3' -> '౩'; '4' -> '౪'; '5' -> '౫'; '6' -> '౬'; '7' -> '౭'; '8' -> '౮'; '9' -> '౯'; else -> char }
            "ml" -> when (char) { '0' -> '൦'; '1' -> '൧'; '2' -> '൨'; '3' -> '൩'; '4' -> '൪'; '5' -> '൫'; '6' -> '൬'; '7' -> '൭'; '8' -> '൮'; '9' -> '൯'; else -> char }
            "ur" -> when (char) { '0' -> '۰'; '1' -> '۱'; '2' -> '۲'; '3' -> '۳'; '4' -> '۴'; '5' -> '۵'; '6' -> '۶'; '7' -> '۷'; '8' -> '۸'; '9' -> '۹'; else -> char }
            else -> char
        }
    }.joinToString("")
}

@Composable
fun LanguageRowItem(name: String, nativeNum: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(text = nativeNum, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}