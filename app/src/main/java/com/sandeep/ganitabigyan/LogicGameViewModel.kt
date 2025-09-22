// FILE: app/src/main/java/com/sandeep/ganitabigyan/LogicGameViewModel.kt

package com.sandeep.ganitabigyan

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class LogicQuestionType(@StringRes val textResId: Int) {
    FIND_ODD(R.string.logic_game_question_odd),
    FIND_EVEN(R.string.logic_game_question_even),
    NUMBER_PATTERN(R.string.logic_game_question_pattern),
    FILL_IN_THE_BLANK(R.string.logic_game_question_fill_blank)
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
    // <<< FIX 1: Instead of a String, we now store the Resource ID and any arguments >>>
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

    fun onAnswerSelected(selectedNumber: Int) {
        if (_uiState.value.isAnswered) return

        val isCorrect = (selectedNumber == _uiState.value.correctAnswer)
        val newScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score

        val milestone = when (newScore) {
            10, 20, 50, 100 -> newScore
            else -> null
        }

        // <<< FIX 2: We now set the feedbackResId instead of hardcoded text >>>
        if (isCorrect) {
            _uiState.update {
                it.copy(
                    score = newScore,
                    isAnswered = true,
                    milestoneReached = milestone,
                    feedbackResId = R.string.feedback_correct_n, // Use the ID for "Correct!"
                    feedbackArgs = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isAnswered = true,
                    feedbackResId = R.string.feedback_incorrect_n, // Use the ID for "Try again!"
                    feedbackArgs = listOf(_uiState.value.correctAnswer) // Pass the number as an argument
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

    private fun generateNewQuestion() {
        val questionType = LogicQuestionType.values().random()
        when (questionType) {
            LogicQuestionType.FIND_ODD, LogicQuestionType.FIND_EVEN -> generateOddEvenQuestion(questionType)
            LogicQuestionType.NUMBER_PATTERN -> generateNumberPatternQuestion()
            LogicQuestionType.FILL_IN_THE_BLANK -> generateFillInTheBlankQuestion()
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

    private fun generatePattern(length: Int = 4): PatternDetails {
        val start = Random.nextInt(1, 10)
        val step = Random.nextInt(2, 6)
        val sequence = List(length) { index -> start + (index * step) }
        val answer = start + (length * step)
        return PatternDetails(sequence = sequence, answer = answer)
    }

    private fun generateRandomOddNumber(): Int = Random.nextInt(0, 50) * 2 + 1
    private fun generateRandomEvenNumber(): Int = (Random.nextInt(1, 51)) * 2
}