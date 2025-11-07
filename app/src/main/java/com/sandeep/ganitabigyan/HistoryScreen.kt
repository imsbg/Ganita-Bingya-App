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
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toLocaleDate
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface HistoryEntry { val date: Date }

// <<< ADD hintUsed to the MathHistoryEntry data class >>>
data class MathHistoryEntry(
    val question: String,
    val userAnswer: String,
    val correctAnswer: String,
    val result: String,
    val hintUsed: Boolean, // New property
    override val date: Date
) : HistoryEntry

data class LogicHistoryEntry(val questionTypeResId: Int, val options: List<String>, val selectedAnswer: String, val correctAnswer: String, val result: String, override val date: Date) : HistoryEntry
data class FtmHistoryEntry(val sequence: List<String>, val options: List<String>, val selectedAnswer: String, val correctAnswer: String, val result: String, override val date: Date) : HistoryEntry
data class WordProblemHistoryEntry(val question: String, val userAnswer: String, val correctAnswer: String, val result: String, val hintUsed: Boolean, override val date: Date) : HistoryEntry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryContent() {
    val context = LocalContext.current
    var historyList by remember { mutableStateOf<List<HistoryEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        val mathHistory = parseMathHistory(context) // This parser is now updated
        val logicHistory = parseLogicHistory(context)
        val ftmnHistory = parseFtmHistory(context)
        val wordProblemHistory = parseWordProblemHistory(context)
        historyList = (mathHistory + logicHistory + ftmnHistory + wordProblemHistory).sortedByDescending { it.date }
    }

    val groupedHistory = historyList.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(it.date) }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (groupedHistory.isEmpty()) {
            item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.history_no_history_found)) } }
        } else {
            groupedHistory.forEach { (dateStr, items) ->
                stickyHeader { Text(text = dateStr.toLocaleDate(context), style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)).padding(vertical = 8.dp, horizontal = 16.dp)) }
                items(items) { item ->
                    when (item) {
                        is MathHistoryEntry -> MathHistoryCard(item = item) // This card is now updated
                        is LogicHistoryEntry -> LogicHistoryCard(item = item)
                        is FtmHistoryEntry -> FtmHistoryCard(item = item)
                        is WordProblemHistoryEntry -> WordProblemHistoryCard(item = item)
                    }
                }
            }
        }
    }
}

// <<< UPDATE MathHistoryCard to show the hint icon >>>
@Composable
private fun MathHistoryCard(item: MathHistoryEntry) {
    val context = LocalContext.current
    val correctResultId = stringResource(R.string.qna_log_result_correct_id)
    val resultIsCorrect = item.result == correctResultId
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

// <<< UPDATE the parseMathHistory function >>>
private fun parseMathHistory(context: Context): List<MathHistoryEntry> {
    val items = mutableListOf<MathHistoryEntry>()
    try {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/qna.gba")
        if (!file.exists()) return emptyList()
        val keyQuestion = context.getString(R.string.qna_log_question_key) + ": "
        val keyYourAnswer = context.getString(R.string.qna_log_your_answer_key) + ": "
        val keyCorrectAnswer = context.getString(R.string.qna_log_correct_answer_key) + ": "
        val keyResult = context.getString(R.string.qna_log_result_key) + ": "
        val keyHintUsed = context.getString(R.string.wp_log_hint_used_key) + ": " // Reuse string
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val lines = file.readLines().filter { it.isNotBlank() }

        // Logic to handle both old (5 lines) and new (6 lines) formats
        var i = 0
        while (i < lines.size) {
            try {
                if (!lines[i].startsWith("[")) { i++; continue } // Skip malformed lines

                val date = dateFormat.parse(lines[i].trim().removeSurrounding("[", "]")) ?: Date()
                val question = lines.getOrNull(i + 1)?.substringAfter(keyQuestion, "") ?: ""
                val userAnswer = lines.getOrNull(i + 2)?.substringAfter(keyYourAnswer, "") ?: ""
                val correctAnswer = lines.getOrNull(i + 3)?.substringAfter(keyCorrectAnswer, "") ?: ""
                val result = lines.getOrNull(i + 4)?.substringAfter(keyResult, "") ?: ""

                // Check if the next line is a hint line or a new entry
                val nextLineIsHint = lines.getOrNull(i + 5)?.startsWith(keyHintUsed.trim()) == true
                val hintUsed = if (nextLineIsHint) {
                    lines[i + 5].substringAfter(keyHintUsed).toBoolean()
                } else {
                    false // Default to false for old entries
                }

                if (question.isNotBlank()) {
                    items.add(MathHistoryEntry(question, userAnswer, correctAnswer, result, hintUsed, date))
                }

                i += if (nextLineIsHint) 6 else 5 // Move index by 6 for new entries, 5 for old
                while (i < lines.size && lines[i].isBlank()) i++ // Skip blank lines
            } catch (e: Exception) {
                i++ // Move to the next line on error to avoid infinite loop
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return items
}

// --- All other cards and parsers are unchanged ---
@Composable private fun LogicHistoryCard(item:LogicHistoryEntry){val c=LocalContext.current;val r=stringResource(R.string.qna_log_result_correct_id);val i=item.result==r;val o=if(i)Color(0xFF4CAF50)else Color(0xFFF44336);Card(modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(text=stringResource(R.string.history_logic_question_type),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f));Text(text=if(item.questionTypeResId!=0)stringResource(id=item.questionTypeResId)else"",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(8.dp));Text(text=stringResource(R.string.history_options,item.options.joinToString{it.toLocaleNumerals(c)}),style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(4.dp));Text(text=stringResource(R.string.history_you_picked,item.selectedAnswer.toLocaleNumerals(c)),style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Bold,color=if(i)Color(0xFF4CAF50)else Color(0xFFF44336));if(!i){Text(text=stringResource(R.string.history_correct_choice,item.correctAnswer.toLocaleNumerals(c)),style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Bold,color=Color(0xFF4CAF50))}}}}
@Composable private fun FtmHistoryCard(item:FtmHistoryEntry){val c=LocalContext.current;val r=stringResource(R.string.qna_log_result_correct_id);val i=item.result==r;val o=if(i)Color(0xFF4CAF50)else Color(0xFFF44336);Card(modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(text=stringResource(R.string.history_ftmn_sequence),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.7f));val s=item.sequence.joinToString(" , "){if(it=="null")"__" else it.toLocaleNumerals(c)};Text(text=s,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(8.dp));Text(text=stringResource(R.string.history_options,item.options.joinToString{it.toLocaleNumerals(c)}),style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(4.dp));Text(text=stringResource(R.string.history_you_picked,item.selectedAnswer.toLocaleNumerals(c)),style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Bold,color=o);if(!i){Text(text=stringResource(R.string.history_correct_choice,item.correctAnswer.toLocaleNumerals(c)),style=MaterialTheme.typography.bodyLarge,fontWeight=FontWeight.Bold,color=Color(0xFF4CAF50))}}}}
@Composable private fun WordProblemHistoryCard(item:WordProblemHistoryEntry){val c=LocalContext.current;val r=stringResource(R.string.qna_log_result_correct_id);val i=item.result==r;val d=if(i)stringResource(R.string.qna_log_correct)else stringResource(R.string.qna_log_incorrect);val o=if(i)Color(0xFF4CAF50)else Color(0xFFF44336);Card(modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)){Column(Modifier.padding(16.dp)){Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Top){Text(text=item.question,style=MaterialTheme.typography.titleMedium,modifier=Modifier.weight(1f));if(item.hintUsed){Icon(imageVector=Icons.Default.Lightbulb,contentDescription=stringResource(R.string.history_hint_used),tint=Color(0xFFFFC107),modifier=Modifier.padding(start=8.dp))}}
    Spacer(Modifier.height(8.dp));Text(stringResource(R.string.history_your_answer,item.userAnswer.toLocaleNumerals(c)));Text(stringResource(R.string.history_correct_answer,item.correctAnswer.toLocaleNumerals(c)));Spacer(Modifier.height(8.dp));Text(text=stringResource(R.string.history_result,d),color=o,style=MaterialTheme.typography.bodyLarge)}}}
private fun parseLogicHistory(c:Context):List<LogicHistoryEntry>{val items=mutableListOf<LogicHistoryEntry>();try{val f=File(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"GanitaBigyan/logic_history.gba");if(!f.exists())return emptyList();val kQT=c.getString(R.string.logic_log_question_type_key)+": ";val kO=c.getString(R.string.logic_log_options_key)+": ";val kS=c.getString(R.string.logic_log_selected_answer_key)+": ";val kC=c.getString(R.string.logic_log_correct_answer_key)+": ";val kR=c.getString(R.string.logic_log_result_key)+": ";val dF=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);val lines=f.readLines().filter{it.isNotBlank()};for(i in lines.indices step 6){if(i+5<lines.size){try{val d=dF.parse(lines[i].trim().removeSurrounding("[","]"))?:Date();val qTN=lines[i+1].substringAfter(kQT);val qTR=c.resources.getIdentifier(qTN,"string",c.packageName);val o=lines[i+2].substringAfter(kO).split(',');val s=lines[i+3].substringAfter(kS);val C=lines[i+4].substringAfter(kC);val r=lines[i+5].substringAfter(kR);items.add(LogicHistoryEntry(qTR,o,s,C,r,d))}catch(e:Exception){}}}}catch(e:Exception){e.printStackTrace()};return items}
private fun parseFtmHistory(c:Context):List<FtmHistoryEntry>{val items=mutableListOf<FtmHistoryEntry>();try{val f=File(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"GanitaBigyan/ftmn_history.gba");if(!f.exists())return emptyList();val kS=c.getString(R.string.ftmn_log_sequence_key)+": ";val kO=c.getString(R.string.logic_log_options_key)+": ";val kSel=c.getString(R.string.logic_log_selected_answer_key)+": ";val kC=c.getString(R.string.logic_log_correct_answer_key)+": ";val kR=c.getString(R.string.logic_log_result_key)+": ";val dF=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);val lines=f.readLines().filter{it.isNotBlank()};for(i in lines.indices step 6){if(i+5<lines.size){try{val d=dF.parse(lines[i].trim().removeSurrounding("[","]"))?:Date();val s=lines[i+1].substringAfter(kS).split(',');val o=lines[i+2].substringAfter(kO).split(',');val sel=lines[i+3].substringAfter(kSel);val C=lines[i+4].substringAfter(kC);val r=lines[i+5].substringAfter(kR);items.add(FtmHistoryEntry(s,o,sel,C,r,d))}catch(e:Exception){}}}}catch(e:Exception){e.printStackTrace()};return items}
private fun parseWordProblemHistory(c:Context):List<WordProblemHistoryEntry>{val items=mutableListOf<WordProblemHistoryEntry>();try{val f=File(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"GanitaBigyan/word_problem_history.gba");if(!f.exists())return emptyList();val kQ=c.getString(R.string.wp_log_question_key)+": ";val kYA=c.getString(R.string.wp_log_your_answer_key)+": ";val kCA=c.getString(R.string.wp_log_correct_answer_key)+": ";val kR=c.getString(R.string.wp_log_result_key)+": ";val kH=c.getString(R.string.wp_log_hint_used_key)+": ";val dF=SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);val lines=f.readLines().filter{it.isNotBlank()};for(i in lines.indices step 6){if(i+5<lines.size){try{val d=dF.parse(lines[i].trim().removeSurrounding("[","]"))?:Date();val q=lines[i+1].substringAfter(kQ);val yA=lines[i+2].substringAfter(kYA);val cA=lines[i+3].substringAfter(kCA);val r=lines[i+4].substringAfter(kR);val h=lines[i+5].substringAfter(kH).toBoolean();items.add(WordProblemHistoryEntry(q,yA,cA,r,h,d))}catch(e:Exception){}}}}catch(e:Exception){e.printStackTrace()};return items}