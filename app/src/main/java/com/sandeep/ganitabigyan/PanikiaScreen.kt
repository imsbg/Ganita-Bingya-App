// FILE: app/src/main/java/com/sandeep/ganitabigyan/PanikiaScreen.kt
// VERSION: FINAL - Supports starting on a specific view via deep link.

package com.sandeep.ganitabigyan

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.ganitabigyan.utils.PanikiaRow
import com.sandeep.ganitabigyan.utils.getPanikiaTable
import com.sandeep.ganitabigyan.utils.toLocaleNumerals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanikiaListScreen(
    onTableClick: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.panikia_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        }
    ) { padding ->
        val context = LocalContext.current
        val tableNumbers = (2..25).toList()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 90.dp),
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tableNumbers) { number ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onTableClick(number) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = number.toLocaleNumerals(context),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PanikiaDetailScreen(
    tableNumber: Int,
    // <<< NEW PARAMETER: Added to accept the view type from the nav graph >>>
    initialView: String?,
    onNavigateBack: () -> Unit
) {
    val totalTablesToDisplay = 25 - tableNumber + 1
    val pageCount = totalTablesToDisplay * 2

    // <<< FIX: Set the initial page based on the deep link parameter >>>
    // If the initialView is "word", start on page 1 (the script view). Otherwise, start on page 0.
    val initialPage = if (initialView == "word") 1 else 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )
    val context = LocalContext.current

    val currentTableForTitle by remember {
        derivedStateOf { tableNumber + (pagerState.currentPage / 2) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.panikia_title_for_table, currentTableForTitle.toLocaleNumerals(context)))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentTableNumber = tableNumber + (page / 2)
                val isNumericalView = page % 2 == 0
                val panikiaTableData = getPanikiaTable(currentTableNumber, context)
                if (isNumericalView) {
                    NumericalTableView(panikiaTable = panikiaTableData)
                } else {
                    ScriptTableView(panikiaTable = panikiaTableData)
                }
            }

            if (pagerState.currentPage < pageCount - 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val swipeUpText = stringResource(R.string.swipe_up_hint)
                    Text(
                        text = swipeUpText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = swipeUpText,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun NumericalTableView(panikiaTable: List<PanikiaRow>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        panikiaTable.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.numericalExpression,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "=",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = row.numericalResult,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider()
        }
    }
}


@Composable
fun ScriptTableView(panikiaTable: List<PanikiaRow>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        panikiaTable.forEach { row ->
            Text(
                text = row.scriptLine,
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.Center
            )
            HorizontalDivider()
        }
    }
}