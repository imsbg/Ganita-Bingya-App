// FILE: app/src/main/java/com/sandeep/ganitabigyan/NumberScreen.kt
// VERSION: FINAL - Fully multilingual.

package com.sandeep.ganitabigyan

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
// <<< CHANGE 1: Import the new, multilingual function >>>
import com.sandeep.ganitabigyan.utils.getNumberList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(
    onNavigateBack: () -> Unit
) {
    // <<< CHANGE 2: Get the context to know the current language >>>
    val context = LocalContext.current

    // <<< CHANGE 3: Call the new function and pass the context >>>
    // We use 'remember' so the list is not regenerated on every recomposition.
    val numberList = remember { getNumberList(context) }

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
            // No changes are needed here! The UI code works perfectly with the new data.
            items(numberList) { numberItem ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = numberItem.numeral,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        text = numberItem.word,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                HorizontalDivider()
            }
        }
    }
}