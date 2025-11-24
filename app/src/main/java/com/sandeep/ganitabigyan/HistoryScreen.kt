// FILE: app/src/main/java/com/sandeep/ganitabigyan/HistoryScreen.kt

package com.sandeep.ganitabigyan

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toLocaleDate
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface HistoryEntry { val date: Date }

data class MathHistoryEntry(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val result: String,
    val hintUsed: Boolean,
    override val date: Date
) : HistoryEntry

data class LogicHistoryEntry(
    val questionTypeResId: Int,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val result: String,
    override val date: Date
) : HistoryEntry

data class FtmHistoryEntry(
    val sequence: List<String>,
    val options: List<String>,
    val selectedAnswer: String,
    val correctAnswer: String,
    val result: String,
    override val date: Date
) : HistoryEntry

data class WordProblemHistoryEntry(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val result: String,
    val hintUsed: Boolean,
    override val date: Date,
    val savedLang: String?
) : HistoryEntry


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent() {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        val mathHistory = parseMathHistory(context)
        val logicHistory = parseLogicHistory(context)
        val ftmnHistory = parseFtmHistory(context)
        val wordProblemHistory = parseWordProblemHistory(context)
        historyList = (mathHistory + logicHistory + ftmnHistory + wordProblemHistory).sortedByDescending { it.date }
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
                    when (item) {
                        is MathHistoryEntry -> MathHistoryCard(item = item)
                        is LogicHistoryEntry -> LogicHistoryCard(item = item)
                        is FtmHistoryEntry -> FtmHistoryCard(item = item)
                        is WordProblemHistoryEntry -> WordProblemHistoryCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun WordProblemHistoryCard(item: WordProblemHistoryEntry) {
    val context = LocalContext.current

    val resultIsCorrect = remember(item.result, item.savedLang) {
        val correctIdentifierForComparison = if (item.savedLang != null) {
            try {
                val config = android.content.res.Configuration(context.resources.configuration)
                config.setLocale(Locale(item.savedLang))
                val localizedContext = context.createConfigurationContext(config)
                localizedContext.getString(R.string.qna_log_result_correct_id)
            } catch (e: Exception) {
                context.getString(R.string.qna_log_result_correct_id)
            }
        } else {
            context.getString(R.string.qna_log_result_correct_id)
        }
        item.result == correctIdentifierForComparison
    }

    val resultDisplayText = if (resultIsCorrect) stringResource(R.string.qna_log_correct) else stringResource(R.string.qna_log_incorrect)
    val resultColor = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)

    // FIXED: Using a more robust check to identify the mismatch message.
    val mismatchIdentifier = remember {
        context.getString(R.string.history_language_mismatch).substringAfter("%s ", "")
    }
    val isLanguageMismatch = if (mismatchIdentifier.isNotEmpty()) {
        item.question.contains(mismatchIdentifier)
    } else {
        // Fallback for the unlikely case the string format changes or has no space
        item.question.startsWith(context.getString(R.string.history_language_mismatch, "").substringBefore(" %s"))
    }


    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = if (isLanguageMismatch) Alignment.CenterVertically else Alignment.Top
            ) {
                Text(
                    text = item.question,
                    style = if(isLanguageMismatch) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = if(isLanguageMismatch) TextAlign.Center else TextAlign.Start,
                    fontWeight = if(isLanguageMismatch) FontWeight.Bold else FontWeight.Normal
                )
                if (item.hintUsed && !isLanguageMismatch) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = stringResource(R.string.history_hint_used),
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            if (!isLanguageMismatch) {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.history_your_answer_label, item.userAnswer.toLocaleNumerals(context)))
                Text(stringResource(R.string.history_correct_answer_label, item.correctAnswer.toLocaleNumerals(context)))
                Spacer(Modifier.height(8.dp))
                Text(text = stringResource(R.string.history_result_label, resultDisplayText), color = resultColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MathHistoryCard(item: MathHistoryEntry) {
    val context = LocalContext.current
    val resultIsCorrect = item.result == context.getString(R.string.qna_log_result_correct_id)
    val resultDisplayText = if (resultIsCorrect) stringResource(R.string.qna_log_correct) else stringResource(R.string.qna_log_incorrect)
    val resultColor = if (resultIsCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stringResource(R.string.history_question_label, item.question.toLocaleNumerals(context)),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                if (item.hintUsed) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = stringResource(R.string.history_hint_used),
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.history_your_answer_label, item.userAnswer.toLocaleNumerals(context)))
            Text(stringResource(R.string.history_correct_answer_label, item.correctAnswer.toLocaleNumerals(context)))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.history_result_label, resultDisplayText),
                color = resultColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LogicHistoryCard(item: LogicHistoryEntry) {
    val context = LocalContext.current
    val resultIsCorrect = item.result == context.getString(R.string.qna_log_result_correct_id)
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
                color = resultColor
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

@Composable
private fun FtmHistoryCard(item: FtmHistoryEntry) {
    val context = LocalContext.current
    val resultIsCorrect = item.result == context.getString(R.string.qna_log_result_correct_id)
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

private fun parseMathHistory(context: Context): List<MathHistoryEntry> {
    val items = mutableListOf<MathHistoryEntry>()
    try {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/qna.gba")
        if (!file.exists()) return emptyList()
        val keyQuestion = context.getString(R.string.qna_log_question_key) + ": "
        val keyYourAnswer = context.getString(R.string.qna_log_your_answer_key) + ": "
        val keyCorrectAnswer = context.getString(R.string.qna_log_correct_answer_key) + ": "
        val keyResult = context.getString(R.string.qna_log_result_key) + ": "
        val keyHintUsed = context.getString(R.string.wp_log_hint_used_key) + ": "
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val lines = file.readLines().filter { it.isNotBlank() }

        var i = 0
        while (i < lines.size) {
            try {
                if (!lines[i].startsWith("[")) { i++; continue }

                val date = dateFormat.parse(lines[i].trim().removeSurrounding("[", "]")) ?: Date()
                val question = lines.getOrNull(i + 1)?.substringAfter(keyQuestion, "") ?: ""
                val userAnswer = lines.getOrNull(i + 2)?.substringAfter(keyYourAnswer, "") ?: ""
                val correctAnswer = lines.getOrNull(i + 3)?.substringAfter(keyCorrectAnswer, "") ?: ""
                val result = lines.getOrNull(i + 4)?.substringAfter(keyResult, "") ?: ""

                val nextLineIsHint = lines.getOrNull(i + 5)?.startsWith(keyHintUsed.trim()) == true
                val hintUsed = if (nextLineIsHint) lines[i + 5].substringAfter(keyHintUsed).toBoolean() else false

                if (question.isNotBlank()) {
                    items.add(MathHistoryEntry(question, userAnswer, correctAnswer, result, hintUsed, date))
                }

                i += if (nextLineIsHint) 6 else 5
                while (i < lines.size && lines[i].isBlank()) i++
            } catch (e: Exception) {
                i++
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return items
}

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

private fun parseWordProblemHistory(context: Context): List<WordProblemHistoryEntry> {
    val items = mutableListOf<WordProblemHistoryEntry>()
    try {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/word_problem_history.gba")
        if (!file.exists()) return emptyList()

        val keyQuestion = context.getString(R.string.wp_log_question_key) + ": "
        val keyYourAnswer = context.getString(R.string.wp_log_your_answer_key) + ": "
        val keyCorrectAnswer = context.getString(R.string.wp_log_correct_answer_key) + ": "
        val keyResult = context.getString(R.string.wp_log_result_key) + ": "
        val keyHintUsed = context.getString(R.string.wp_log_hint_used_key) + ": "
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val lines = file.readLines().filter { it.isNotBlank() }
        val currentAppLang = context.resources.configuration.locales[0].language

        for (i in lines.indices step 6) {
            if (i + 5 < lines.size) {
                try {
                    val date = dateFormat.parse(lines[i].trim().removeSurrounding("[", "]")) ?: Date()

                    val questionLine = lines[i + 1].substringAfter(keyQuestion)
                    val userAnswer = lines[i + 2].substringAfter(keyYourAnswer)
                    val correctAnswer = lines[i + 3].substringAfter(keyCorrectAnswer)
                    val result = lines[i + 4].substringAfter(keyResult)
                    val hintUsed = lines[i + 5].substringAfter(keyHintUsed).toBoolean()

                    var formattedQuestion = ""
                    val parts = questionLine.split("|")
                    val resIdString = parts.getOrNull(0)?.substringAfter("resId:")
                    val numbersString = parts.getOrNull(1)?.substringAfter("numbers:")
                    val savedLang = parts.getOrNull(2)?.substringAfter("lang:")

                    if (resIdString != null && numbersString != null && savedLang != null) {
                        if (currentAppLang == savedLang) {
                            val resId = resIdString.toInt()
                            val numbers = numbersString.split(",").mapNotNull { it.trim().toIntOrNull() }
                            var questionTemplate = context.getString(resId)
                            numbers.forEachIndexed { index, num ->
                                questionTemplate = questionTemplate.replace("{${index + 1}}", num.toLocaleNumerals(context))
                            }
                            formattedQuestion = questionTemplate
                        } else {
                            val languageName = Locale(savedLang).getDisplayLanguage(Locale(currentAppLang))
                            formattedQuestion = context.getString(R.string.history_language_mismatch, languageName)
                        }
                    } else { formattedQuestion = questionLine }

                    items.add(WordProblemHistoryEntry(formattedQuestion, userAnswer, correctAnswer, result, hintUsed, date, savedLang))

                } catch (e: Exception) { /* Skip malformed entries */ }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return items
}