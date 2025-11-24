// FILE: app/src/main/java/com/sandeep/ganitabigyan/OrdinalsScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.ui.common.AutoResizeText
import com.sandeep.ganitabigyan.utils.toLocaleNumerals

private data class OrdinalEntry(val number: String, val word: String, val symbol: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OrdinalsScreen(navController: NavController) {
    val context = LocalContext.current

    val numbers = stringArrayResource(id = R.array.ordinal_numbers)
    val words = stringArrayResource(id = R.array.ordinal_words)
    val symbols = stringArrayResource(id = R.array.ordinal_symbols)

    val ordinalData = numbers.indices.map { index ->
        OrdinalEntry(
            number = numbers[index].toLocaleNumerals(context),
            word = words[index],
            symbol = symbols[index]
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.ordinals_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            stickyHeader {
                OrdinalRow(
                    number = stringResource(id = R.string.header_number),
                    word = stringResource(id = R.string.header_word),
                    symbol = stringResource(id = R.string.header_symbol),
                    isHeader = true
                )
            }

            items(ordinalData) { entry ->
                OrdinalRow(
                    number = entry.number,
                    word = entry.word,
                    symbol = entry.symbol
                )
                Divider()
            }
        }
    }
}

@Composable
private fun OrdinalRow(number: String, word: String, symbol: String, isHeader: Boolean = false) {
    val background = if (isHeader) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal

    // <<< FIX: Define an adaptive text color based on the background >>>
    val textColor = if (isHeader) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AutoResizeText(
            text = number,
            modifier = Modifier.weight(1f),
            // <<< FIX: Apply the adaptive text color >>>
            color = textColor,
            style = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontWeight = fontWeight
            )
        )
        AutoResizeText(
            text = word,
            modifier = Modifier.weight(2f),
            // <<< FIX: Apply the adaptive text color >>>
            color = textColor,
            style = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontWeight = fontWeight
            )
        )
        AutoResizeText(
            text = symbol,
            modifier = Modifier.weight(1f),
            // <<< FIX: Apply the adaptive text color >>>
            color = textColor,
            style = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontWeight = fontWeight
            )
        )
    }
}