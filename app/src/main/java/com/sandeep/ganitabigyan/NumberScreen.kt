// FILE: app/src/main/java/com/sandeep/ganitabigyan/NumberScreen.kt

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import com.sandeep.ganitabigyan.utils.toWords

// Data class for our list
data class NumberItem(
    val value: Int,
    val numeral: String,
    val word: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Generate the specific list you requested
    val numberList = remember { generateExtendedNumberList(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.numbers_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(numberList) { numberItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // The Digit (e.g., 150 / ୧୫୦)
                    Text(
                        text = numberItem.numeral,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.width(80.dp)
                    )

                    // The Word (e.g., One Hundred Fifty / ଦେଢ଼ ଶହ)
                    Text(
                        text = numberItem.word,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .weight(1f)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

// Helper to generate the sequence: 0-100, 150, 200... 1500... 1 Lakh
private fun generateExtendedNumberList(context: Context): List<NumberItem> {
    val list = mutableListOf<NumberItem>()

    // 1. Add 0 to 100
    for (i in 0..100) {
        list.add(createItem(context, i))
    }

    // 2. Add specific large numbers
    val largeNumbers = listOf(
        150, 200, 250, 300, 400, 500, 600, 700, 800, 900,
        1000, 1500, 2000, 5000, 10000, 100000
    )

    for (num in largeNumbers) {
        list.add(createItem(context, num))
    }

    return list
}

private fun createItem(context: Context, number: Int): NumberItem {
    val numeral = number.toLocaleNumerals(context)
    val word = getSpecialNumberWord(context, number)
    return NumberItem(number, numeral, word)
}

// Logic to handle words
private fun getSpecialNumberWord(context: Context, number: Int): String {
    // 1. Standard 0-100
    if (number <= 100) {
        return number.toWords(context)
    }

    // 2. SPECIAL NUMBERS (150, 250, 1500)
    // These load directly from strings.xml so you can customize them per language
    if (number == 150) return context.getString(R.string.word_150_special)
    if (number == 250) return context.getString(R.string.word_250_special)
    // <<< NEW CHANGE: Special check for 1500 >>>
    if (number == 1500) return context.getString(R.string.word_1500_special)

    // 3. Hundreds (200, 300... 900)
    if (number in 200..900 && number % 100 == 0) {
        val digit = number / 100
        val digitWord = digit.toWords(context)
        val hundredSuffix = context.getString(R.string.word_hundreds_suffix)
        return "$digitWord $hundredSuffix"
    }

    // 4. Thousands (1000, 2000, 5000, 10000)
    if (number >= 1000 && number < 100000) {
        val thousandVal = number / 1000
        val thousandWord = thousandVal.toWords(context)
        val thousandSuffix = context.getString(R.string.word_thousand)
        return "$thousandWord $thousandSuffix"
    }

    // 5. Lakh (100,000)
    if (number == 100000) {
        val one = 1.toWords(context)
        val lakh = context.getString(R.string.word_lakh)
        return "$one $lakh"
    }

    return number.toString()
}