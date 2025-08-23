// HistoryScreen.kt

package com.sandeep.ganitabigyan

import android.os.Environment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toOdiaNumerals // Import the number converter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

data class HistoryItem(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val result: String,
    val date: String
)

fun formatOdiaDate(dateStr: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("or", "IN"))
    return try {
        val date = inputFormat.parse(dateStr)
        val formattedDateWithEnglishNumerals = outputFormat.format(date!!)
        formattedDateWithEnglishNumerals.toOdiaNumerals()
    } catch (e: Exception) {
        dateStr
    }
}

// CHANGE: The original HistoryScreen is now just "HistoryContent".
// It no longer has a Scaffold or takes a navigation callback.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent() {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
        val file = File(ganitaBigyanDir, "qna.gba")
        if (file.exists()) {
            val lines = file.readLines().filter { it.isNotBlank() }
            val items = mutableListOf<HistoryItem>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

            for (i in lines.indices step 5) {
                if (i + 4 < lines.size) {
                    try {
                        val timestampLine = lines[i]
                        val date = dateFormat.parse(timestampLine.trim().removeSurrounding("[", "]"))
                        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date!!)

                        val question = lines[i + 1].substringAfter("ପ୍ରଶ୍ନ: ")
                        val userAnswer = lines[i + 2].substringAfter("ତୁମର ଉତ୍ତର: ")
                        val correctAnswer = lines[i + 3].substringAfter("ସଠିକ ଉତ୍ତର: ")
                        val result = lines[i + 4].substringAfter("ଫଳାଫଳ: ")
                        items.add(HistoryItem(question, userAnswer, correctAnswer, result, dateString))
                    } catch (e: Exception) {
                        // Skip malformed entries
                    }
                }
            }
            historyList = items.reversed()
        }
    }

    val groupedHistory = historyList.groupBy { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (groupedHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("କୌଣସି ଇତିହାସ ମିଳିଲା ନାହିଁ")
                }
            }
        } else {
            groupedHistory.forEach { (date, items) ->
                stickyHeader {
                    Text(
                        text = formatOdiaDate(date),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }

                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(item.question, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("ତୁମର ଉତ୍ତର: ${item.userAnswer}")
                            Text("ସଠିକ ଉତ୍ତର: ${item.correctAnswer}")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = item.result,
                                color = if (item.result == "ଠିକ୍") Color(0xFF4CAF50) else Color(0xFFF44336),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}