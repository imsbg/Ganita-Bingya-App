// FILE: app/src/main/java/com/sandeep/ganitabigyan/FTMNViewModel.kt

package com.sandeep.ganitabigyan

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.ganitabigyan.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

// isGameOver is removed from the state
data class FTMNUiState(
    val questionSequenceWithNull: List<Int?> = emptyList(),
    val answerOptions: List<Int> = emptyList(),
    val selectedAnswer: Int? = null,
    val isAnswerChecked: Boolean = false,
    val isAnswerCorrect: Boolean = false,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val isSolutionVisible: Boolean = false,
    val solutionExplanationResId: Int = 0,
    val solutionExplanationValue: Int? = null,
    private val correctAnswer: Int = 0
) {
    fun getCorrectAnswer(): Int = correctAnswer
}

private data class MissingNumberQuestion(
    val sequenceWithNull: List<Int?>,
    val answer: Int,
    val explanationResId: Int,
    val explanationValue: Int? = null
)

private data class FtmHistoryItem(
    val sequence: String,
    val options: String,
    val selectedAnswer: String,
    val correctAnswer: String,
    val result: String
)

class FTMNViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FTMNUiState())
    val uiState: StateFlow<FTMNUiState> = _uiState.asStateFlow()

    // No more totalQuestions or sessionHistory list

    init {
        generateNewQuestion()
    }

    fun onOptionSelected(answer: Int) {
        if (!_uiState.value.isAnswerChecked) {
            _uiState.update { it.copy(selectedAnswer = answer) }
        }
    }

    fun checkAnswer(context: Context) {
        val currentState = _uiState.value
        if (currentState.isAnswerChecked) return

        val isCorrect = currentState.selectedAnswer == currentState.getCorrectAnswer()

        // Create the history item for this single question
        val sequenceString = currentState.questionSequenceWithNull.joinToString(",") { it?.toString() ?: "null" }
        val optionsString = currentState.answerOptions.joinToString(",")
        val resultString = if (isCorrect) context.getString(R.string.qna_log_result_correct_id) else context.getString(R.string.qna_log_result_incorrect_id)

        val historyItem = FtmHistoryItem(
            sequence = sequenceString,
            options = optionsString,
            selectedAnswer = currentState.selectedAnswer?.toString() ?: "N/A",
            correctAnswer = currentState.getCorrectAnswer().toString(),
            result = resultString
        )

        // <<< FIX: Save this single item to history immediately >>>
        saveSingleHistoryItem(context, historyItem)

        // <<< FIX: Update the lifetime score file immediately >>>
        updateLifetimeScore(context, isCorrect)

        // Update the UI state for the current session
        _uiState.update {
            it.copy(
                isAnswerChecked = true,
                isAnswerCorrect = isCorrect,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                wrongCount = if (!isCorrect) it.wrongCount + 1 else it.wrongCount
            )
        }
    }

    fun nextQuestion() {
        generateNewQuestion()
    }

    fun toggleSolutionVisibility() {
        _uiState.update { it.copy(isSolutionVisible = !it.isSolutionVisible) }
    }

    private fun generateNewQuestion() {
        viewModelScope.launch {
            val question = QuestionGenerator.generateRandomQuestion()
            val options = generateAnswerOptions(question.answer)
            _uiState.update {
                it.copy(
                    questionSequenceWithNull = question.sequenceWithNull,
                    correctAnswer = question.answer,
                    solutionExplanationResId = question.explanationResId,
                    solutionExplanationValue = question.explanationValue,
                    answerOptions = options,
                    selectedAnswer = null,
                    isAnswerChecked = false,
                    isAnswerCorrect = false,
                    isSolutionVisible = false
                )
            }
        }
    }

    // <<< NEW: This function updates the lifetime score file by +1 >>>
    private fun updateLifetimeScore(context: Context, wasCorrect: Boolean) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val ganitaBigyanDir = File(documentsDir, "GanitaBigyan").apply { mkdirs() }
            val scoreFile = File(ganitaBigyanDir, "ftmn_lifetime_score.gba")

            var currentCorrect = 0
            var currentIncorrect = 0
            if (scoreFile.exists()) {
                val parts = scoreFile.readText().split(",")
                if (parts.size == 2) {
                    currentCorrect = parts[0].toIntOrNull() ?: 0
                    currentIncorrect = parts[1].toIntOrNull() ?: 0
                }
            }

            if (wasCorrect) {
                currentCorrect++
            } else {
                currentIncorrect++
            }

            scoreFile.writeText("$currentCorrect,$currentIncorrect")
        } catch (e: Exception) { e.printStackTrace() }
    }

    // <<< NEW: This function saves just one item to the history file >>>
    private fun saveSingleHistoryItem(context: Context, item: FtmHistoryItem) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val ganitaBigyanDir = File(documentsDir, "GanitaBigyan").apply { mkdirs() }
            val historyFile = File(ganitaBigyanDir, "ftmn_history.gba")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            val stringBuilder = StringBuilder()

            stringBuilder.appendLine("[${dateFormat.format(Date())}]")
            stringBuilder.appendLine("${context.getString(R.string.ftmn_log_sequence_key)}: ${item.sequence}")
            stringBuilder.appendLine("${context.getString(R.string.logic_log_options_key)}: ${item.options}")
            stringBuilder.appendLine("${context.getString(R.string.logic_log_selected_answer_key)}: ${item.selectedAnswer}")
            stringBuilder.appendLine("${context.getString(R.string.logic_log_correct_answer_key)}: ${item.correctAnswer}")
            stringBuilder.appendLine("${context.getString(R.string.logic_log_result_key)}: ${item.result}")
            stringBuilder.appendLine()

            historyFile.appendText(stringBuilder.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun generateAnswerOptions(correctAnswer: Int): List<Int> {
        val options = mutableSetOf(correctAnswer); val numberOfOptions = 4; while (options.size < numberOfOptions) { val range = (abs(correctAnswer) / 4).coerceIn(5, 20); val distractor = correctAnswer + Random.nextInt(-range, range); if (distractor != correctAnswer) { options.add(distractor) } }; return options.toList().shuffled()
    }
}

private object QuestionGenerator {
    fun generateRandomQuestion(): MissingNumberQuestion { val generators = listOf(::generateSimpleAddition, ::generateSimpleSubtraction, ::generateSimpleMultiplication, ::generateSimpleDivision, ::generateIncreasingDifference, ::generateFibonacciStyle); return generators.random().invoke() }
    private fun generateSimpleAddition(): MissingNumberQuestion { val start = Random.nextInt(1, 20); val diff = Random.nextInt(2, 12); val sequence = List(5) { start + it * diff }; return hideOneElement(sequence, R.string.ftmn_solution_add, diff) }
    private fun generateSimpleSubtraction(): MissingNumberQuestion { val end = Random.nextInt(1, 20); val diff = Random.nextInt(2, 12); val sequence = List(5) { end + it * diff }.reversed(); return hideOneElement(sequence, R.string.ftmn_solution_subtract, diff) }
    private fun generateSimpleMultiplication(): MissingNumberQuestion { val start = Random.nextInt(1, 5); val ratio = Random.nextInt(2, 5); val sequence = generateSequence(start) { it * ratio }.take(5).toList(); return hideOneElement(sequence, R.string.ftmn_solution_multiply, ratio) }
    private fun generateSimpleDivision(): MissingNumberQuestion { val end = Random.nextInt(1, 5); val ratio = Random.nextInt(2, 4); val sequence = generateSequence(end) { it * ratio }.take(5).toList().reversed(); return hideOneElement(sequence, R.string.ftmn_solution_divide, ratio) }
    private fun generateIncreasingDifference(): MissingNumberQuestion { var current = Random.nextInt(1, 10); var diff = Random.nextInt(1, 4); val sequence = mutableListOf<Int>(); repeat(5) { sequence.add(current); current += diff; diff++ }; return hideOneElement(sequence, R.string.ftmn_solution_increasing_diff, null) }
    private fun generateFibonacciStyle(): MissingNumberQuestion { val a = Random.nextInt(1, 5); val b = Random.nextInt(a + 1, 10); val sequence = generateSequence(a to b) { it.second to it.first + it.second }.map { it.first }.take(6).toList(); return hideOneElement(sequence, R.string.ftmn_solution_fibonacci, null) }
    private fun hideOneElement(sequence: List<Int>, explanationResId: Int, explanationValue: Int?): MissingNumberQuestion { val indexToHide = Random.nextInt(0, sequence.size); val answer = sequence[indexToHide]; val displaySequence = sequence.map { it as Int? }.toMutableList(); displaySequence[indexToHide] = null; return MissingNumberQuestion(sequenceWithNull = displaySequence, answer = answer, explanationResId = explanationResId, explanationValue = explanationValue) }
}