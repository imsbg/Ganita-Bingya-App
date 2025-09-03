// FILE: app/src/main/java/com/sandeep/ganitabigyan/HistoryScreen.kt
// VERSION: FINAL - Reads stable keys and translates for display. Fixes all bugs.

package com.sandeep.ganitabigyan

import android.os.Environment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toLocaleDate
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

data class HistoryItem(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val result: String, // This will now hold "correct_id" or "incorrect_id"
    val date: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent() {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }

    // Use the STABLE, NON-TRANSLATABLE keys to parse the log file
    val keyQuestion = stringResource(R.string.qna_log_question_key) + ": "
    val keyYourAnswer = stringResource(R.string.qna_log_your_answer_key) + ": "
    val keyCorrectAnswer = stringResource(R.string.qna_log_correct_answer_key) + ": "
    val keyResult = stringResource(R.string.qna_log_result_key) + ": "

    LaunchedEffect(Unit) {
        try {
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

                            val question = lines[i + 1].substringAfter(keyQuestion)
                            val userAnswer = lines[i + 2].substringAfter(keyYourAnswer)
                            val correctAnswer = lines[i + 3].substringAfter(keyCorrectAnswer)
                            val result = lines[i + 4].substringAfter(keyResult)
                            items.add(HistoryItem(question, userAnswer, correctAnswer, result, dateString))
                        } catch (e: Exception) { /* Skip malformed entries */ }
                    }
                }
                historyList = items.reversed()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    val groupedHistory = historyList.groupBy { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (groupedHistory.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.history_no_history_found))
                }
            }
        } else {
            groupedHistory.forEach { (date, items) ->
                stickyHeader {
                    Text(
                        text = date.toLocaleDate(context),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }

                items(items) { item ->
                    // Get the STABLE identifier for "correct" to compare against
                    val correctResultId = stringResource(R.string.qna_log_result_correct_id)

                    // Determine the display text and color based on the stable ID
                    val resultIsCorrect = item.result == correctResultId
                    val resultDisplayText = if (resultIsCorrect) {
                        stringResource(R.string.qna_log_correct)
                    } else {
                        stringResource(R.string.qna_log_incorrect)
                    }
                    val resultColor = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.history_question_label, item.question.toLocaleNumerals(context)),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.history_your_answer, item.userAnswer.toLocaleNumerals(context)))
                            Text(stringResource(R.string.history_correct_answer, item.correctAnswer.toLocaleNumerals(context)))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.history_result, resultDisplayText),
                                color = resultColor,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}