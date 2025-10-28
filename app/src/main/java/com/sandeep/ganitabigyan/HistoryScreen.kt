// FILE: app/src/main/java/com/sandeep/ganitabigyan/HistoryScreen.kt

package com.sandeep.ganitabigyan

import android.content.Context
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toLocaleDate
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// A sealed interface is a modern way to handle different types of related data.
sealed interface HistoryEntry {
    val date: Date
}

// Data class for the simple Math Game history
data class MathHistoryEntry(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val result: String,
    override val date: Date
) : HistoryEntry

// Data class for the Logic Game history
data class LogicHistoryEntry(
    val questionTypeResId: Int,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val result: String,
    override val date: Date
) : HistoryEntry

// Data class for the new Find the Missing Number Game history
data class FtmHistoryEntry(
    val sequence: List<String>,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val result: String,
    override val date: Date
) : HistoryEntry


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent() {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }

    // This effect now reads from all three history files
    LaunchedEffect(Unit) {
        val mathHistory = parseMathHistory(context)
        val logicHistory = parseLogicHistory(context)
        val ftmnHistory = parseFtmHistory(context)
        // Combine all lists and sort by date
        historyList = (mathHistory + logicHistory + ftmnHistory).sortedByDescending { it.date }
    }

    val groupedHistory = historyList.groupBy {
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(it.date)
    }

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
            groupedHistory.forEach { (dateStr, items) ->
                stickyHeader {
                    Text(
                        text = dateStr.toLocaleDate(context),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }

                items(items) { item ->
                    // A 'when' statement intelligently chooses the correct card for each history type
                    when (item) {
                        is MathHistoryEntry -> MathHistoryCard(item = item)
                        is LogicHistoryEntry -> LogicHistoryCard(item = item)
                        is FtmHistoryEntry -> FtmHistoryCard(item = item)
                    }
                }
            }
        }
    }
}

// This is your original card for the Math Game, it's perfect.
@Composable
private fun MathHistoryCard(item: MathHistoryEntry) {
    val context = LocalContext.current
    val correctResultId = stringResource(R.string.qna_log_result_correct_id)
    val resultIsCorrect = item.result == correctResultId
    val resultDisplayText = if (resultIsCorrect) stringResource(R.string.qna_log_correct) else stringResource(R.string.qna_log_incorrect)
    val resultColor = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
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

// This is the new card for the Logic Game, matching your screenshot
@Composable
private fun LogicHistoryCard(item: LogicHistoryEntry) {
    val context = LocalContext.current
    val correctResultId = stringResource(R.string.qna_log_result_correct_id)
    val resultIsCorrect = item.result == correctResultId
    val resultColor = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.history_logic_question_type),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = if (item.questionTypeResId != 0) stringResource(id = item.questionTypeResId) else "",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.history_options, item.options.joinToString { it.toLocaleNumerals(context) }),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.history_you_picked, item.selectedAnswer.toLocaleNumerals(context)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            if (!resultIsCorrect) {
                Text(
                    text = stringResource(R.string.history_correct_choice, item.correctAnswer.toLocaleNumerals(context)),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

// This is the new card for the "Find the Missing Number" game
@Composable
private fun FtmHistoryCard(item: FtmHistoryEntry) {
    val context = LocalContext.current
    val correctResultId = stringResource(R.string.qna_log_result_correct_id)
    val resultIsCorrect = item.result == correctResultId
    val resultColor = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(R.string.history_ftmn_sequence), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            val sequenceText = item.sequence.joinToString(" , ") {
                if (it == "null") "__" else it.toLocaleNumerals(context)
            }
            Text(text = sequenceText, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(R.string.history_options, item.options.joinToString { it.toLocaleNumerals(context) }), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = stringResource(R.string.history_you_picked, item.selectedAnswer.toLocaleNumerals(context)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = resultColor)
            if (!resultIsCorrect) {
                Text(text = stringResource(R.string.history_correct_choice, item.correctAnswer.toLocaleNumerals(context)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            }
        }
    }
}


// This is the parser for the Math Game file (qna.gba)
private fun parseMathHistory(context: Context): List<MathHistoryEntry> {
    val items = mutableListOf<MathHistoryEntry>()
    try {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/qna.gba")
        if (!file.exists()) return emptyList()
        val keyQuestion = context.getString(R.string.qna_log_question_key) + ": "
        val keyYourAnswer = context.getString(R.string.qna_log_your_answer_key) + ": "
        val keyCorrectAnswer = context.getString(R.string.qna_log_correct_answer_key) + ": "
        val keyResult = context.getString(R.string.qna_log_result_key) + ": "
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val lines = file.readLines().filter { it.isNotBlank() }
        for (i in lines.indices step 5) {
            if (i + 4 < lines.size) {
                try {
                    val date = dateFormat.parse(lines[i].trim().removeSurrounding("[", "]")) ?: Date()
                    val question = lines[i + 1].substringAfter(keyQuestion)
                    val userAnswer = lines[i + 2].substringAfter(keyYourAnswer)
                    val correctAnswer = lines[i + 3].substringAfter(keyCorrectAnswer)
                    val result = lines[i + 4].substringAfter(keyResult)
                    items.add(MathHistoryEntry(question, userAnswer, correctAnswer, result, date))
                } catch (e: Exception) { /* Skip malformed entries */ }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return items
}

// This is the new parser for the Logic Game file (logic_history.gba)
private fun parseLogicHistory(context: Context): List<LogicHistoryEntry> {
    val items = mutableListOf<LogicHistoryEntry>()
    try {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/logic_history.gba")
        if (!file.exists()) return emptyList()
        val keyQuestionType = context.getString(R.string.logic_log_question_type_key) + ": "
        val keyOptions = context.getString(R.string.logic_log_options_key) + ": "
        val keySelected = context.getString(R.string.logic_log_selected_answer_key) + ": "
        val keyCorrect = context.getString(R.string.logic_log_correct_answer_key) + ": "
        val keyResult = context.getString(R.string.logic_log_result_key) + ": "
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val lines = file.readLines().filter { it.isNotBlank() }
        for (i in lines.indices step 6) {
            if (i + 5 < lines.size) {
                try {
                    val date = dateFormat.parse(lines[i].trim().removeSurrounding("[", "]")) ?: Date()
                    val questionTypeName = lines[i + 1].substringAfter(keyQuestionType)
                    val questionTypeResId = context.resources.getIdentifier(questionTypeName, "string", context.packageName)
                    val options = lines[i + 2].substringAfter(keyOptions).split(',')
                    val selected = lines[i + 3].substringAfter(keySelected)
                    val correct = lines[i + 4].substringAfter(keyCorrect)
                    val result = lines[i + 5].substringAfter(keyResult)
                    items.add(LogicHistoryEntry(questionTypeResId, options, selected, correct, result, date))
                } catch (e: Exception) { /* Skip malformed entries */ }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return items
}

// This is the new parser for the "Find the Missing Number" file (ftmn_history.gba)
private fun parseFtmHistory(context: Context): List<FtmHistoryEntry> {
    val items = mutableListOf<FtmHistoryEntry>()
    try {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/ftmn_history.gba")
        if (!file.exists()) return emptyList()
        val keySequence = context.getString(R.string.ftmn_log_sequence_key) + ": "
        val keyOptions = context.getString(R.string.logic_log_options_key) + ": "
        val keySelected = context.getString(R.string.logic_log_selected_answer_key) + ": "
        val keyCorrect = context.getString(R.string.logic_log_correct_answer_key) + ": "
        val keyResult = context.getString(R.string.logic_log_result_key) + ": "
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val lines = file.readLines().filter { it.isNotBlank() }
        for (i in lines.indices step 6) {
            if (i + 5 < lines.size) {
                try {
                    val date = dateFormat.parse(lines[i].trim().removeSurrounding("[", "]")) ?: Date()
                    val sequence = lines[i + 1].substringAfter(keySequence).split(',')
                    val options = lines[i + 2].substringAfter(keyOptions).split(',')
                    val selected = lines[i + 3].substringAfter(keySelected)
                    val correct = lines[i + 4].substringAfter(keyCorrect)
                    val result = lines[i + 5].substringAfter(keyResult)
                    items.add(FtmHistoryEntry(sequence, options, selected, correct, result, date))
                } catch (e: Exception) { /* Skip malformed entries */ }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return items
}