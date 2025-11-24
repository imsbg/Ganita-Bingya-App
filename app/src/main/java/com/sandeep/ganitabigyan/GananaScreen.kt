// FILE: app/src/main/java/com/sandeep/ganitabigyan/GananaScreen.kt

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

private data class GananaEntry(val number: String, val name: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GananaScreen(navController: NavController) {
    val context = LocalContext.current

    val numbers = stringArrayResource(id = R.array.ganana_numbers)
    val names = stringArrayResource(id = R.array.ganana_names)

    val gananaData = numbers.indices.map { index ->
        GananaEntry(
            number = numbers[index].toLocaleNumerals(context),
            name = names[index]
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.ganana_screen_title)) },
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
                GananaRow(
                    number = stringResource(id = R.string.header_number),
                    name = stringResource(id = R.string.header_name),
                    isHeader = true
                )
            }

            items(gananaData) { entry ->
                GananaRow(
                    number = entry.number,
                    name = entry.name
                )
                Divider()
            }
        }
    }
}

@Composable
private fun GananaRow(number: String, name: String, isHeader: Boolean = false) {
    val background = if (isHeader) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AutoResizeText(
            text = number,
            modifier = Modifier.weight(1.5f), // Give a bit more space for large numbers
            color = textColor,
            style = LocalTextStyle.current.copy(
                textAlign = TextAlign.Start,
                fontWeight = fontWeight
            )
        )
        AutoResizeText(
            text = name,
            modifier = Modifier.weight(2f), // Give more space for the name
            color = textColor,
            style = LocalTextStyle.current.copy(
                textAlign = TextAlign.End,
                fontWeight = fontWeight
            )
        )
    }
}