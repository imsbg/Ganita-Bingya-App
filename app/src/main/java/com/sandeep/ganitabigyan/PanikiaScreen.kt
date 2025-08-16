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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.ganitabigyan.utils.PanikiaFullRow
import com.sandeep.ganitabigyan.utils.getOdiaPanikiaTable
import com.sandeep.ganitabigyan.utils.toOdia

// PanikiaListScreen remains the same, no changes.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanikiaListScreen(
    onTableClick: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ପଣିକିଆ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
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
                            text = number.toOdia(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// PanikiaDetailScreen is completely redesigned for the new flow.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PanikiaDetailScreen(
    tableNumber: Int, // This is the STARTING table number
    onNavigateBack: () -> Unit
) {
    // Calculate the total number of pages needed.
    // Each table (from the selected one up to 25) has 2 pages.
    val totalTablesToDisplay = 25 - tableNumber + 1
    val pageCount = totalTablesToDisplay * 2
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // This clever bit updates the title bar automatically as you swipe!
    val currentTableForTitle by remember {
        derivedStateOf { tableNumber + (pagerState.currentPage / 2) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${currentTableForTitle.toOdia()} ପଣିକିଆ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                // Determine which table and which format to show for the current page
                val currentTableNumber = tableNumber + (page / 2)
                val isNumericalView = page % 2 == 0
                val panikiaTableData = getOdiaPanikiaTable(currentTableNumber)

                if (isNumericalView) {
                    NumericalOdiaTableView(panikiaTable = panikiaTableData)
                } else {
                    ScriptOdiaTableView(panikiaTable = panikiaTableData)
                }
            }

            // Show a "Swipe Up" hint on all pages except the very last one.
            if (pagerState.currentPage < pageCount - 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "ଉପରକୁ ସ୍ୱାଇପ୍ କରନ୍ତୁ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "ଉପରକୁ ସ୍ୱାଇପ୍ କରନ୍ତୁ",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// A dedicated composable for the Odia Numerical table view.
@Composable
fun NumericalOdiaTableView(panikiaTable: List<PanikiaFullRow>) {
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

// A dedicated composable for the Odia Script table view.
@Composable
fun ScriptOdiaTableView(panikiaTable: List<PanikiaFullRow>) {
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