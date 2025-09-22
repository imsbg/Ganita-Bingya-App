// FILE: app/src/main/java/com/sandeep/ganitabigyan/VisualGameViewModel.kt

package com.sandeep.ganitabigyan

import androidx.annotation.DrawableRes
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

enum class FruitType(@DrawableRes val imageResId: Int) {
    APPLE(R.drawable.game_apple),
    ORANGE(R.drawable.game_orange),
    MANGO(R.drawable.game_mango)
}

data class FruitGroup(
    val type: FruitType,
    val quantity: Int
)

enum class QuestionType(@StringRes val textResId: Int) {
    MORE(R.string.visual_game_question_more),
    LESS(R.string.visual_game_question_less)
}

data class VisualGameState(
    val questionNumber: Int = 0,
    val score: Int = 0,
    val fruitGroups: List<FruitGroup> = emptyList(),
    val isAnswered: Boolean = false,
    val currentQuestion: QuestionType = QuestionType.MORE,
    // <<< FIX 1: Upgraded to use multilingual strings and milestones >>>
    @StringRes val feedbackResId: Int? = null,
    val feedbackArgs: List<Any> = emptyList(),
    val milestoneReached: Int? = null
)

class VisualGameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VisualGameState())
    val uiState: StateFlow<VisualGameState> = _uiState.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        _uiState.value = VisualGameState()
        generateNewQuestion()
    }

    fun onAnswerSelected(selectedGroup: FruitGroup) {
        if (_uiState.value.isAnswered) return

        val correctAnswer = if (_uiState.value.currentQuestion == QuestionType.MORE) {
            _uiState.value.fruitGroups.maxByOrNull { it.quantity }
        } else {
            _uiState.value.fruitGroups.minByOrNull { it.quantity }
        }

        val isCorrect = selectedGroup == correctAnswer
        val newScore = if (isCorrect) _uiState.value.score + 1 else _uiState.value.score

        // <<< NEW: Check for milestones >>>
        val milestone = when (newScore) {
            10, 20, 50, 100 -> newScore
            else -> null
        }

        // <<< FIX 2: Set the feedback using string resource IDs >>>
        if (isCorrect) {
            _uiState.update {
                it.copy(
                    score = newScore,
                    isAnswered = true,
                    milestoneReached = milestone,
                    feedbackResId = R.string.feedback_correct,
                    feedbackArgs = emptyList()
                )
            }
        } else {
            // Updated feedback to use a more generic message for more/less
            val incorrectFeedback = if (_uiState.value.currentQuestion == QuestionType.MORE) {
                "Try again! The answer was ${correctAnswer?.quantity}."
            } else {
                "Try again! The answer was ${correctAnswer?.quantity}."
            }
            _uiState.update {
                it.copy(
                    isAnswered = true,
                    feedbackResId = R.string.feedback_incorrect, // Using a generic feedback string
                    feedbackArgs = listOf(correctAnswer?.quantity ?: 0)
                )
            }
        }

        viewModelScope.launch {
            delay(1500)
            generateNewQuestion() // <<< FIX 3: Game is now unlimited >>>
        }
    }

    fun onMilestoneAcknowledged() {
        _uiState.update { it.copy(milestoneReached = null) }
    }

    private fun generateNewQuestion() {
        val possibleNumbers = (1..10).toMutableList()
        possibleNumbers.shuffle()
        val quantities = listOf(possibleNumbers[0], possibleNumbers[1], possibleNumbers[2])

        val groups = listOf(
            FruitGroup(FruitType.APPLE, quantities[0]),
            FruitGroup(FruitType.ORANGE, quantities[1]),
            FruitGroup(FruitType.MANGO, quantities[2])
        ).shuffled()

        val nextQuestionType = if (Random.nextBoolean()) QuestionType.MORE else QuestionType.LESS

        _uiState.update {
            it.copy(
                questionNumber = it.questionNumber + 1,
                fruitGroups = groups,
                isAnswered = false,
                currentQuestion = nextQuestionType,
                feedbackResId = null,
                feedbackArgs = emptyList()
            )
        }
    }
}