// FILE: app/src/main/java/com/sandeep/ganitabigyan/RomanNumbersScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomanNumbersScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- List State ---
    var currentLimit by remember { mutableIntStateOf(100) }
    val maxLimit = 1000 // Start button limit
    val absoluteMaxRoman = 3999 // Mathematical limit for standard Roman numerals

    // --- Search State ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    // Generate the normal list
    val numberList = remember(currentLimit) { (1..currentLimit).toList() }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                // --- SEARCH BAR MODE ---
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { newValue ->
                                // Allow both English and Odia digits
                                if (newValue.all { it.isDigit() || isOdiaDigit(it) }) {
                                    searchQuery = newValue
                                }
                            },
                            placeholder = { Text(stringResource(R.string.search_hint), fontSize = 14.sp) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    keyboardController?.hide()
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            // Close Search
                            isSearchActive = false
                            searchQuery = ""
                            keyboardController?.hide()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_close_search))
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
                // Auto-focus the keyboard when search opens
                LaunchedEffect(Unit) {
                    searchFocusRequester.requestFocus()
                }
            } else {
                // --- NORMAL TITLE MODE ---
                TopAppBar(
                    title = { Text(stringResource(R.string.roman_screen_title)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button_description)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // --- Header Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.col_number),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.col_roman),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // --- CONTENT AREA ---
            if (isSearchActive && searchQuery.isNotEmpty()) {
                // *** SEARCH RESULT VIEW ***
                val parsedNumber = parseOdiaOrEnglishInt(searchQuery)

                if (parsedNumber in 1..absoluteMaxRoman) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            // UPDATED LINE HERE
                            text = stringResource(R.string.search_result_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RomanNumberRow(parsedNumber, context)
                        }
                    }
                } else {
                    // Not found / Error
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.search_no_result),
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // *** NORMAL LIST VIEW ***
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // The Items
                    items(numberList, key = { it }) { number ->
                        RomanNumberRow(number, context)
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }

                    // The "Load More" Button at the bottom
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (currentLimit < maxLimit) {
                            Button(
                                onClick = {
                                    currentLimit += 100
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(100)
                                        listState.animateScrollToItem(currentLimit - 95)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_load_more))
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.msg_list_complete),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RomanNumberRow(number: Int, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Localized Number (e.g., ୧୦, १०, 10)
        Text(
            text = number.toString().toLocaleNumerals(context),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        // Roman Number (e.g., X)
        Text(
            text = toRoman(number),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// --- LOGIC: Auto-Convert Int to Roman (Supports up to 3999) ---
fun toRoman(num: Int): String {
    if (num <= 0 || num >= 4000) return ""
    val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var number = num
    val sb = StringBuilder()
    var i = 0
    while (number > 0) {
        while (number >= values[i]) {
            number -= values[i]
            sb.append(symbols[i])
        }
        i++
    }
    return sb.toString()
}

// --- LOGIC: Check if char is Odia digit ---
fun isOdiaDigit(char: Char): Boolean {
    return char in '୦'..'୯'
}

// --- LOGIC: Parse Mixed (English/Odia) String to Int ---
fun parseOdiaOrEnglishInt(input: String): Int {
    val englishString = input.map { char ->
        when (char) {
            '୦' -> '0'
            '୧' -> '1'
            '୨' -> '2'
            '୩' -> '3'
            '୪' -> '4'
            '୫' -> '5'
            '୬' -> '6'
            '୭' -> '7'
            '୮' -> '8'
            '୯' -> '9'
            else -> char // Keep English digits as they are
        }
    }.joinToString("")

    return englishString.toIntOrNull() ?: -1
}