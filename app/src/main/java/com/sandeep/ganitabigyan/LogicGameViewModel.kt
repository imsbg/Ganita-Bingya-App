// FILE: app/src/main/java/com/sandeep/ganitabigyan/LogicGameViewModel.kt
// PASTE THIS ENTIRE, CORRECTED CODE INTO YOUR FILE

package com.sandeep.ganitabigyan

import android.content.Context
import android.os.Environment
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// --- The rest of the file is unchanged, only saveLogicHistory is modified ---
enum class LogicQuestionType(@StringRes val textResId: Int) {
    FIND_ODD(R.string.logic_game_question_odd),
    FIND_EVEN(R.string.logic_game_question_even),
    NUMBER_PATTERN(R.string.logic_game_question_pattern),
    FILL_IN_THE_BLANK(R.string.logic_game_question_fill_blank),
    FIND_BIG_EVEN(R.string.logic_game_question_big_even),
    FIND_SMALL_ODD(R.string.logic_game_question_small_odd),
    FIND_BIG_ODD(R.string.logic_game_question_big_odd),
    FIND_SMALL_EVEN(R.string.logic_game_question_small_even),
    DIVISIBLE_BY_10(R.string.logic_game_question_divisible_by_10),
    ENDS_WITH_5(R.string.logic_game_question_ends_in_5),
    ENDS_WITH_3(R.string.logic_game_question_ends_in_3),
    STARTS_WITH_5(R.string.logic_game_question_starts_with_5),
    STARTS_1_ENDS_3(R.string.logic_game_question_starts_1_ends_3),
    STARTS_WITH_9(R.string.logic_game_question_starts_with_9),
    STARTS_WITH_3(R.string.logic_game_question_starts_with_3),
    PICK_THE_LARGEST(R.string.logic_game_question_pick_largest),
    PICK_THE_SMALLEST(R.string.logic_game_question_pick_smallest),
    STARTS_WITH_7(R.string.logic_game_question_starts_with_7),
    ENDS_WITH_1(R.string.logic_game_question_ends_with_1),
    STARTS_8_ENDS_8(R.string.logic_game_question_starts_8_ends_8),
    TWO_IDENTICAL_DIGITS(R.string.logic_game_question_two_identical_digits)
}
data class PatternDetails(val sequence: List<Int>, val answer: Int)
data class LogicGameState(
    val questionNumber: Int = 0,
    val score: Int = 0,
    val questionType: LogicQuestionType = LogicQuestionType.FIND_ODD,
    val questionText: String = "",
    val answerOptions: List<Int> = emptyList(),
    val correctAnswer: Int = 0,
    val isAnswered: Boolean = false,
    val milestoneReached: Int? = null,
    @StringRes val feedbackResId: Int? = null,
    val feedbackArgs: List<Any> = emptyList()
)

class LogicGameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LogicGameState())
    val uiState: StateFlow<LogicGameState> = _uiState.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        _uiState.value = LogicGameState()
        generateNewQuestion()
    }

    fun onAnswerSelected(selectedNumber: Int, context: Context) {
        if (_uiState.value.isAnswered) return

        val currentState = _uiState.value
        val isCorrect = (selectedNumber == currentState.correctAnswer)
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        viewModelScope.launch {
            saveLogicHistory(
                context = context,
                questionTypeResId = currentState.questionType.textResId,
                options = currentState.answerOptions,
                selectedAnswer = selectedNumber,
                correctAnswer = currentState.correctAnswer,
                isCorrect = isCorrect
            )
            updateLogicLifetimeScore(context, isCorrect)
        }

        val milestone = when (newScore) {
            10, 20, 50, 100 -> newScore
            else -> null
        }

        if (isCorrect) {
            _uiState.update {
                it.copy(
                    score = newScore,
                    isAnswered = true,
                    milestoneReached = milestone,
                    feedbackResId = R.string.feedback_correct_n,
                    feedbackArgs = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isAnswered = true,
                    feedbackResId = R.string.feedback_incorrect_n,
                    feedbackArgs = listOf(currentState.correctAnswer)
                )
            }
        }

        viewModelScope.launch {
            delay(1500)
            generateNewQuestion()
        }
    }

    fun onMilestoneAcknowledged() {
        _uiState.update { it.copy(milestoneReached = null) }
    }

    private suspend fun updateLogicLifetimeScore(context: Context, wasCorrect: Boolean) = withContext(Dispatchers.IO) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
            if (!ganitaBigyanDir.exists()) ganitaBigyanDir.mkdirs()
            val file = File(ganitaBigyanDir, "logic_lifetime_score.gba")

            var correctCount = 0
            var incorrectCount = 0

            if (file.exists()) {
                val parts = file.readText().split(",")
                if (parts.size == 2) {
                    correctCount = parts[0].toIntOrNull() ?: 0
                    incorrectCount = parts[1].toIntOrNull() ?: 0
                }
            }

            if (wasCorrect) {
                correctCount++
            } else {
                incorrectCount++
            }

            file.writeText("$correctCount,$incorrectCount")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun saveLogicHistory(
        context: Context,
        @StringRes questionTypeResId: Int,
        options: List<Int>,
        selectedAnswer: Int,
        correctAnswer: Int,
        isCorrect: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
            if (!ganitaBigyanDir.exists()) ganitaBigyanDir.mkdirs()
            val file = File(ganitaBigyanDir, "logic_history.gba")

            val questionTypeKey = context.getString(R.string.logic_log_question_type_key)
            val optionsKey = context.getString(R.string.logic_log_options_key)
            val selectedAnswerKey = context.getString(R.string.logic_log_selected_answer_key)
            val correctAnswerKey = context.getString(R.string.logic_log_correct_answer_key)
            val resultKey = context.getString(R.string.logic_log_result_key)
            val resultValue = if (isCorrect) context.getString(R.string.qna_log_result_correct_id) else context.getString(R.string.qna_log_result_incorrect_id)

            // <<< THE FIX IS HERE: We now save the resource NAME instead of the English text >>>
            val questionTypeName = context.resources.getResourceEntryName(questionTypeResId)

            val optionsText = options.joinToString(",")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())

            val entry = """
                [${timestamp}]
                $questionTypeKey: $questionTypeName
                $optionsKey: $optionsText
                $selectedAnswerKey: $selectedAnswer
                $correctAnswerKey: $correctAnswer
                $resultKey: $resultValue
                
            """.trimIndent()
            file.appendText("\n" + entry)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- All generate question functions below remain unchanged ---
    private fun generateNewQuestion() {
        val questionType = LogicQuestionType.values().random()
        when (questionType) {
            LogicQuestionType.FIND_ODD, LogicQuestionType.FIND_EVEN -> generateOddEvenQuestion(questionType)
            LogicQuestionType.NUMBER_PATTERN -> generateNumberPatternQuestion()
            LogicQuestionType.FILL_IN_THE_BLANK -> generateFillInTheBlankQuestion()
            LogicQuestionType.PICK_THE_LARGEST, LogicQuestionType.PICK_THE_SMALLEST -> generateComparisonQuestion(questionType)
            else -> generateSimpleChoiceQuestion(questionType)
        }
    }

    private fun generateOddEvenQuestion(questionType: LogicQuestionType) {
        val correctAnswer = if (questionType == LogicQuestionType.FIND_ODD) generateRandomOddNumber() else generateRandomEvenNumber()
        val incorrectAnswers = mutableSetOf<Int>()
        while (incorrectAnswers.size < 3) {
            val wrongNumber = if (questionType == LogicQuestionType.FIND_ODD) generateRandomEvenNumber() else generateRandomOddNumber()
            if (wrongNumber != correctAnswer) { incorrectAnswers.add(wrongNumber) }
        }
        val allOptions = (incorrectAnswers + correctAnswer).shuffled()
        _uiState.update {
            it.copy(
                questionNumber = it.questionNumber + 1,
                questionType = questionType,
                questionText = "",
                answerOptions = allOptions,
                correctAnswer = correctAnswer,
                isAnswered = false,
                feedbackResId = null,
                feedbackArgs = emptyList()
            )
        }
    }

    private fun generateNumberPatternQuestion() {
        val pattern = generatePattern()
        val incorrectAnswers = mutableSetOf<Int>()
        while (incorrectAnswers.size < 3) {
            val wrongOffset = Random.nextInt(-5, 6)
            if (wrongOffset != 0) { incorrectAnswers.add(pattern.answer + wrongOffset) }
        }
        val allOptions = (incorrectAnswers + pattern.answer).shuffled()
        val questionSequence = pattern.sequence.joinToString(", ") + ", __"
        _uiState.update {
            it.copy(
                questionNumber = it.questionNumber + 1,
                questionType = LogicQuestionType.NUMBER_PATTERN,
                questionText = questionSequence,
                answerOptions = allOptions,
                correctAnswer = pattern.answer,
                isAnswered = false,
                feedbackResId = null,
                feedbackArgs = emptyList()
            )
        }
    }

    private fun generateFillInTheBlankQuestion() {
        val pattern = generatePattern(length = 5)
        val blankIndex = Random.nextInt(1, 4)
        val correctAnswer = pattern.sequence[blankIndex]
        val incorrectAnswers = mutableSetOf<Int>()
        while (incorrectAnswers.size < 3) {
            val wrongOffset = Random.nextInt(-5, 6)
            val wrongAnswer = correctAnswer + wrongOffset
            if (wrongOffset != 0 && !pattern.sequence.contains(wrongAnswer)) {
                incorrectAnswers.add(wrongAnswer)
            }
        }
        val allOptions = (incorrectAnswers + correctAnswer).shuffled()
        val questionSequence = pattern.sequence.mapIndexed { index, number ->
            if (index == blankIndex) "__" else number.toString()
        }.joinToString(", ")
        _uiState.update {
            it.copy(
                questionNumber = it.questionNumber + 1,
                questionType = LogicQuestionType.FILL_IN_THE_BLANK,
                questionText = questionSequence,
                answerOptions = allOptions,
                correctAnswer = correctAnswer,
                isAnswered = false,
                feedbackResId = null,
                feedbackArgs = emptyList()
            )
        }
    }

    private fun generateComparisonQuestion(questionType: LogicQuestionType) {
        val options = mutableSetOf<Int>()
        while (options.size < 4) {
            options.add(Random.nextInt(10, 100))
        }

        val correctAnswer = if (questionType == LogicQuestionType.PICK_THE_LARGEST) {
            options.maxOrNull() ?: 0
        } else {
            options.minOrNull() ?: 0
        }

        _uiState.update {
            it.copy(
                questionNumber = it.questionNumber + 1,
                questionType = questionType,
                questionText = "",
                answerOptions = options.shuffled(),
                correctAnswer = correctAnswer,
                isAnswered = false,
                feedbackResId = null,
                feedbackArgs = emptyList()
            )
        }
    }

    private fun generateSimpleChoiceQuestion(questionType: LogicQuestionType) {
        var correctAnswer: Int
        val incorrectAnswers = mutableSetOf<Int>()

        when (questionType) {
            LogicQuestionType.FIND_BIG_EVEN -> {
                correctAnswer = generateBigEvenNumber()
                while (incorrectAnswers.size < 3) {
                    if (Random.nextBoolean()) incorrectAnswers.add(generateSmallEvenNumber())
                    else incorrectAnswers.add(generateRandomOddNumber())
                }
            }
            LogicQuestionType.FIND_SMALL_ODD -> {
                correctAnswer = generateSmallOddNumber()
                while (incorrectAnswers.size < 3) {
                    if (Random.nextBoolean()) incorrectAnswers.add(generateBigOddNumber())
                    else incorrectAnswers.add(generateRandomEvenNumber())
                }
            }
            LogicQuestionType.FIND_BIG_ODD -> {
                correctAnswer = generateBigOddNumber()
                while (incorrectAnswers.size < 3) {
                    if (Random.nextBoolean()) incorrectAnswers.add(generateSmallOddNumber())
                    else incorrectAnswers.add(generateRandomEvenNumber())
                }
            }
            LogicQuestionType.FIND_SMALL_EVEN -> {
                correctAnswer = generateSmallEvenNumber()
                while (incorrectAnswers.size < 3) {
                    if (Random.nextBoolean()) incorrectAnswers.add(generateBigEvenNumber())
                    else incorrectAnswers.add(generateRandomOddNumber())
                }
            }
            LogicQuestionType.DIVISIBLE_BY_10 -> {
                correctAnswer = (1..10).random() * 10
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(1, 101)
                    if (wrong % 10 != 0) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.ENDS_WITH_5 -> {
                correctAnswer = Random.nextInt(0, 10) * 10 + 5
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(1, 101)
                    if (wrong % 10 != 5) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.ENDS_WITH_3 -> {
                correctAnswer = Random.nextInt(0, 10) * 10 + 3
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(1, 101)
                    if (wrong % 10 != 3) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.STARTS_WITH_5 -> {
                correctAnswer = 50 + Random.nextInt(0, 10)
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong !in 50..59) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.STARTS_WITH_9 -> {
                correctAnswer = 90 + Random.nextInt(0, 10)
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong !in 90..99) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.STARTS_WITH_3 -> {
                correctAnswer = 30 + Random.nextInt(0, 10)
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong !in 30..39) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.STARTS_1_ENDS_3 -> {
                correctAnswer = 13
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong != 13) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.STARTS_WITH_7 -> {
                correctAnswer = 70 + Random.nextInt(0, 10)
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong !in 70..79) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.ENDS_WITH_1 -> {
                correctAnswer = Random.nextInt(0, 10) * 10 + 1
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(1, 101)
                    if (wrong % 10 != 1) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.STARTS_8_ENDS_8 -> {
                correctAnswer = 88
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong != 88) incorrectAnswers.add(wrong)
                }
            }
            LogicQuestionType.TWO_IDENTICAL_DIGITS -> {
                val digit = Random.nextInt(1, 10)
                correctAnswer = digit * 11
                while (incorrectAnswers.size < 3) {
                    val wrong = Random.nextInt(10, 100)
                    if (wrong / 10 != wrong % 10) {
                        incorrectAnswers.add(wrong)
                    }
                }
            }
            else -> return
        }

        incorrectAnswers.remove(correctAnswer)
        while (incorrectAnswers.size < 3) {
            val wrong = Random.nextInt(1, 101)
            if (wrong != correctAnswer) incorrectAnswers.add(wrong)
        }

        val allOptions = (incorrectAnswers + correctAnswer).shuffled()

        _uiState.update {
            it.copy(
                questionNumber = it.questionNumber + 1,
                questionType = questionType,
                questionText = "",
                answerOptions = allOptions,
                correctAnswer = correctAnswer,
                isAnswered = false,
                feedbackResId = null,
                feedbackArgs = emptyList()
            )
        }
    }

    private fun generatePattern(length: Int = 4): PatternDetails {
        val start = Random.nextInt(1, 10)
        val step = Random.nextInt(2, 6)
        val sequence = List(length) { index -> start + (index * step) }
        val answer = start + (length * step)
        return PatternDetails(sequence = sequence, answer = answer)
    }

    private fun generateRandomOddNumber(): Int = Random.nextInt(0, 50) * 2 + 1
    private fun generateRandomEvenNumber(): Int = (Random.nextInt(1, 51)) * 2
    private fun generateBigEvenNumber(): Int = Random.nextInt(25, 50) * 2
    private fun generateSmallEvenNumber(): Int = Random.nextInt(1, 25) * 2
    private fun generateBigOddNumber(): Int = Random.nextInt(25, 50) * 2 + 1
    private fun generateSmallOddNumber(): Int = Random.nextInt(0, 25) * 2 + 1
}