package com.sandeep.ganitabigyan

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.ganitabigyan.utils.toOdia
import com.sandeep.ganitabigyan.utils.toOdiaNumerals
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

data class GameState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val wrongAttempts: Int = 0,
    val correctStreak: Int = 0,
    val selectedType: String = "ମିଶ୍ରଣ",
    val selectedLevel: String = "ସହଜ",
    val isAutoScrollEnabled: Boolean = false,
    val isTimedChallenge: Boolean = false,
    val challengeJustFinished: Boolean = false,
    val timerValue: Long = 0L,
    val feedbackMessage: String? = null,
    val isExtraHardAvailable: Boolean = true,
    val isLoading: Boolean = true
)

data class Question(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val solution: String? = null,
    val userAnswer: String? = null,
    val isAnswered: Boolean = false
)

enum class HapticFeedbackType { INCORRECT }

sealed class UiEvent {
    object ShowTimedChallengeDialog : UiEvent()
    object RequestAutoScroll : UiEvent()
}

class GameViewModel(private val context: Context) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()
    private val _hapticEvent = MutableSharedFlow<HapticFeedbackType>()
    val hapticEvent = _hapticEvent.asSharedFlow()
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()
    private val settingsDataStore = SettingsDataStore(context)
    private var feedbackJob: Job? = null
    private var timerJob: Job? = null
    private var lifetimeScore = Pair(0, 0)

    val gameTypes = listOf("ମିଶ୍ରଣ", "ମିଶାଣ", "ଫେଡାଣ", "ଗୁଣନ", "ହରଣ", "ସମୀକରଣ ଖୋଜ")
    val difficultyLevels = listOf("ସହଜ", "ମଧ୍ୟମ", "କଠିନ", "ଅତି କଠିନ")
    val motivationalQuotes = listOf(
        "ଜୟ ଜଗନ୍ନାଥ", "ଅଧ୍ୟୟନ ହେଉଛି ଶକ୍ତି", "ଆଜି କଣ ପଢିବା?", "ଗଣିତ କରିବା କି?", "ମିଶାଣ କରିବା କି?",
        "ଫେଡାଣ କରିବା କି?", "ଗୁଣନ କରିବା କି?", "ହରଣ କରିବା କି?", "ସମୀକରଣ ଖୋଜିବା କି?", "ଆମେ ଓଡ଼ିଆ ଭାରି ବଢିଆ",
        "ପଢି ଚାଲ, ମାଡି ଚାଲ", "ପାଠ ପଢିବା ଆମର ଦୈନିକ କାମ", "ଆଜି କେତୋଟି ପ୍ରଶ୍ନ ର ଉତ୍ତର ଦେବେ?", "ସ୍ବାଗତ",
        "ନମସ୍କାର", "ଜୟ ଓଡ଼ିଶା ଜୟ ଓଡ଼ିଆ", "ମୁଁ ଓଡ଼ିଆ ବୋଲି ଗର୍ବ କରୁଛି", "ଚାଲ ଆଜି ୫୦ ଟି ପ୍ରଶ୍ନ ର ଉତ୍ତର ଦେବା",
        "ପରିଶ୍ରମ କରିଚାଲ", "ଜ୍ଞାନ ହିଁ ବଳ", "ଚେଷ୍ଟା କଲେ ସବୁ ସମ୍ଭବ", "କର୍ମ ହିଁ ଭଗବାନ", "ଆଉ ସବୁ ଭଲ ତ?", "ଉତ୍ତର ଜଣା ନାହିଁ କି?",
        "ଉପରେ ସମାଧାନ ଦେଖି ପାରିବ"
    )

    init {
        loadSettingsAndStart()
        loadLifetimeScore()
    }

    private fun loadLifetimeScore() {
        viewModelScope.launch {
            try {
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan/lifetime_score.gba")
                if (file.exists()) {
                    val parts = file.readText().split(",")
                    if (parts.size == 2) {
                        lifetimeScore = Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun saveLifetimeScore() {
        viewModelScope.launch {
            try {
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
                if (!ganitaBigyanDir.exists()) ganitaBigyanDir.mkdirs()
                val file = File(ganitaBigyanDir, "lifetime_score.gba")
                file.writeText("${lifetimeScore.first},${lifetimeScore.second}")
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun loadSettingsAndStart() {
        viewModelScope.launch {
            _gameState.update { it.copy(isLoading = true) }
            val savedType = settingsDataStore.gameType.first()
            val savedLevel = settingsDataStore.difficultyLevel.first()
            val savedAutoScroll = settingsDataStore.autoScroll.first()
            _gameState.update {
                it.copy(
                    selectedType = savedType,
                    selectedLevel = savedLevel,
                    isAutoScrollEnabled = savedAutoScroll,
                    isExtraHardAvailable = savedType in listOf("ମିଶ୍ରଣ", "ସମୀକରଣ ଖୋଜ"),
                    isLoading = false
                )
            }
            generateNewQuestionSet()
        }
    }

    fun updateSettings(newType: String, newLevel: String) {
        viewModelScope.launch {
            val isExtraHardAvailable = newType in listOf("ମିଶ୍ରଣ", "ସମୀକରଣ ଖୋଜ")
            val finalLevel = if (!isExtraHardAvailable && newLevel == "ଅତି କଠିନ") "କଠିନ" else newLevel
            _gameState.update {
                it.copy(selectedType = newType, selectedLevel = finalLevel, isExtraHardAvailable = isExtraHardAvailable)
            }
            settingsDataStore.saveSettings(newType, finalLevel)
            generateNewQuestionSet()
        }
    }

    fun generateNewQuestionSet(count: Int = 50) {
        _gameState.update {
            it.copy(
                questions = List(count) { generateQuestion() },
                currentQuestionIndex = 0,
                feedbackMessage = null,
                score = 0,
                wrongAttempts = 0,
                correctStreak = 0
            )
        }
        updateCurrentQuestionIndex(0)
    }

    private fun appendQuestions(count: Int = 20) {
        val newQuestions = List(count) { generateQuestion() }
        _gameState.update { it.copy(questions = it.questions + newQuestions) }
    }

    fun onAnswerSelected(answer: String) {
        val currentState = _gameState.value
        val questionIndex = currentState.currentQuestionIndex
        if (currentState.questions.getOrNull(questionIndex)?.isAnswered == true) return

        val currentQuestion = currentState.questions[questionIndex]
        val isCorrect = answer == currentQuestion.correctAnswer

        val updatedQuestions = currentState.questions.toMutableList()
        updatedQuestions[questionIndex] = currentQuestion.copy(userAnswer = answer, isAnswered = true)

        val newStreak = if (isCorrect) currentState.correctStreak + 1 else 0
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score
        val newWrongAttempts = if (!isCorrect) currentState.wrongAttempts + 1 else currentState.wrongAttempts

        val feedbackMessage = if (isCorrect) {
            if (newStreak >= 3) "ସଠିକ ଉତ୍ତର! ${newStreak.toOdia()} ସିରିଜ୍" else "ବାଃ! ସଠିକ ଉତ୍ତର"
        } else "ଭୁଲ୍ ଉତ୍ତର | ସଠିକ ଉତ୍ତର: ${currentQuestion.correctAnswer}"

        if (isCorrect) {
            lifetimeScore = lifetimeScore.copy(first = lifetimeScore.first + 1)
        } else {
            lifetimeScore = lifetimeScore.copy(second = lifetimeScore.second + 1)
        }
        saveLifetimeScore()

        _gameState.update {
            it.copy(
                questions = updatedQuestions,
                score = newScore,
                wrongAttempts = newWrongAttempts,
                correctStreak = newStreak
            )
        }

        showFeedbackMessage(feedbackMessage)
        viewModelScope.launch {
            if (!isCorrect) _hapticEvent.emit(HapticFeedbackType.INCORRECT)
            saveAnswerToFile(currentQuestion, answer, isCorrect)

            if (_gameState.value.isAutoScrollEnabled || _gameState.value.isTimedChallenge) {
                delay(1500)
                _uiEvent.emit(UiEvent.RequestAutoScroll)
            }
        }
    }

    private fun showFeedbackMessage(message: String) {
        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            _gameState.update { it.copy(feedbackMessage = message) }
            delay(2500)
            _gameState.update { it.copy(feedbackMessage = null) }
        }
    }

    fun moveToNextQuestion() {
        _gameState.update {
            it.copy(currentQuestionIndex = it.currentQuestionIndex + 1)
        }
    }

    fun updateCurrentQuestionIndex(newIndex: Int) {
        if (newIndex >= _gameState.value.questions.size - 5) {
            appendQuestions()
        }
        _gameState.update { it.copy(currentQuestionIndex = newIndex) }
    }

    fun toggleAutoScroll(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.saveAutoScroll(isEnabled)
            _gameState.update { it.copy(isAutoScrollEnabled = isEnabled) }
        }
    }

    fun startTimedChallenge(minutes: Int, type: String, level: String) {
        timerJob?.cancel()
        val durationMillis = minutes * 60 * 1000L
        _gameState.update { it.copy(isTimedChallenge = true, timerValue = durationMillis, score = 0, wrongAttempts = 0, challengeJustFinished = false) }
        updateSettings(type, level)

        timerJob = viewModelScope.launch {
            var timeLeft = durationMillis
            while (timeLeft > 0 && _gameState.value.isTimedChallenge) {
                delay(1000)
                timeLeft -= 1000
                _gameState.update { it.copy(timerValue = timeLeft) }
            }
            if (_gameState.value.isTimedChallenge) {
                _gameState.update { it.copy(isTimedChallenge = false, challengeJustFinished = true) }
            }
        }
    }

    fun stopTimedChallenge() {
        timerJob?.cancel()
        _gameState.update { it.copy(isTimedChallenge = false, timerValue = 0, challengeJustFinished = false) }
        generateNewQuestionSet()
    }

    fun dismissChallengeSummary() {
        _gameState.update { it.copy(challengeJustFinished = false) }
        generateNewQuestionSet()
    }

    fun requestTimedChallengeDialog() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowTimedChallengeDialog)
        }
    }

    fun triggerHapticFeedback(type: HapticFeedbackType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (type == HapticFeedbackType.INCORRECT) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 75, 50, 75), -1))
                }
            } else {
                if (type == HapticFeedbackType.INCORRECT) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 75, 50, 75), -1)
                }
            }
        }
    }

    private fun saveAnswerToFile(question: Question, userAnswer: String, isCorrect: Boolean) {
        viewModelScope.launch {
            try {
                val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
                if (!ganitaBigyanDir.exists()) ganitaBigyanDir.mkdirs()
                val file = File(ganitaBigyanDir, "qna.gba")
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val resultText = if (isCorrect) "ଠିକ୍" else "ଭୁଲ୍"
                val contentToAppend = "[$timestamp]\nପ୍ରଶ୍ନ: ${question.questionText}\nତୁମର ଉତ୍ତର: $userAnswer\nସଠିକ ଉତ୍ତର: ${question.correctAnswer}\nଫଳାଫଳ: $resultText\n\n"
                file.appendText(contentToAppend)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun generateQuestion(): Question {
        val type = if (_gameState.value.selectedType == "ମିଶ୍ରଣ") {
            listOf("ମିଶାଣ", "ଫେଡାଣ", "ଗୁଣନ", "ହରଣ").random()
        } else {
            _gameState.value.selectedType
        }
        return when (type) {
            "ମିଶାଣ" -> generateAddition(); "ଫେଡାଣ" -> generateSubtraction()
            "ଗୁଣନ" -> generateMultiplication(); "ହରଣ" -> generateDivision()
            "ସମୀକରଣ ଖୋଜ" -> generateFindExpression(); else -> generateAddition()
        }
    }

    private fun generateAddition(): Question {
        val (n1, n2) = when (_gameState.value.selectedLevel) {
            "ସହଜ" -> Random.nextInt(1, 10) to Random.nextInt(1, 10)
            "ମଧ୍ୟମ" -> Random.nextInt(10, 100) to Random.nextInt(10, 100)
            "କଠିନ" -> Random.nextInt(100, 1000) to Random.nextInt(100, 1000)
            else -> Random.nextInt(1000, 10000) to Random.nextInt(1000, 10000)
        }
        val answer = n1 + n2; val options = generateOptions(answer)
        val questionText = "${n1.toOdia()} + ${n2.toOdia()} = ?"; val solutionText = "${n1.toOdia()} + ${n2.toOdia()} = ${answer.toOdia()}"
        return Question(questionText, options.map { it.toOdia() }, answer.toOdia(), solution = solutionText)
    }
    private fun generateSubtraction(): Question {
        val (n1, n2) = when (_gameState.value.selectedLevel) {
            "ସହଜ" -> Random.nextInt(5, 20) to Random.nextInt(1, 5)
            "ମଧ୍ୟମ" -> Random.nextInt(20, 100) to Random.nextInt(10, 20)
            "କଠିନ" -> Random.nextInt(100, 1000) to Random.nextInt(10, 100)
            else -> Random.nextInt(100, 1000) to Random.nextInt(10, 100)
        }.let { (a, b) -> if (a > b) a to b else b to a }
        val answer = n1 - n2; val options = generateOptions(answer)
        val questionText = "${n1.toOdia()} - ${n2.toOdia()} = ?"; val solutionText = "${n1.toOdia()} - ${n2.toOdia()} = ${answer.toOdia()}"
        return Question(questionText, options.map { it.toOdia() }, answer.toOdia(), solution = solutionText)
    }
    private fun generateMultiplication(): Question {
        val (n1, n2) = when (_gameState.value.selectedLevel) {
            "ସହଜ" -> Random.nextInt(2, 10) to Random.nextInt(2, 10)
            "ମଧ୍ୟମ" -> Random.nextInt(10, 26) to Random.nextInt(2, 10)
            "କଠିନ" -> Random.nextInt(11, 31) to Random.nextInt(11, 21)
            else -> Random.nextInt(11, 31) to Random.nextInt(11, 21)
        }
        val answer = n1 * n2; val options = generateOptions(answer)
        val questionText = "${n1.toOdia()} × ${n2.toOdia()} = ?"; val solutionText = "${n1.toOdia()} × ${n2.toOdia()} = ${answer.toOdia()}"
        return Question(questionText, options.map { it.toOdia() }, answer.toOdia(), solution = solutionText)
    }
    private fun generateDivision(): Question {
        val (ans, div) = when (_gameState.value.selectedLevel) {
            "ସହଜ" -> Random.nextInt(2, 11) to Random.nextInt(2, 6)
            "ମଧ୍ୟମ" -> Random.nextInt(5, 21) to Random.nextInt(2, 11)
            "କଠିନ" -> Random.nextInt(10, 51) to Random.nextInt(5, 21)
            else -> Random.nextInt(10, 51) to Random.nextInt(5, 21)
        }
        val num = ans * div; val answer = ans; val options = generateOptions(answer)
        val questionText = "${num.toOdia()} ÷ ${div.toOdia()} = ?"; val solutionText = "${num.toOdia()} ÷ ${div.toOdia()} = ${answer.toOdia()}"
        return Question(questionText, options.map { it.toOdia() }, answer.toOdia(), solution = solutionText)
    }
    private fun generateFindExpression(): Question {
        val answer = when (_gameState.value.selectedLevel) {
            "ସହଜ" -> Random.nextInt(5, 21); "ମଧ୍ୟମ" -> Random.nextInt(20, 101)
            "କଠିନ" -> Random.nextInt(50, 201); else -> Random.nextInt(100, 501)
        }
        val correctExpr = generateExpression(answer).first
        val wrongExpr1 = generateExpression(answer + (Random.nextInt(-20, 20).takeIf { it != 0 } ?: 1)).first
        val wrongExpr2 = generateExpression(answer + (Random.nextInt(-20, 20).takeIf { it != 0 } ?: 2)).first
        val wrongExpr3 = generateExpression(answer + (Random.nextInt(-20, 20).takeIf { it != 0 } ?: 3)).first
        val options = listOf(correctExpr, wrongExpr1, wrongExpr2, wrongExpr3).map { it.toOdiaNumerals() }.shuffled()
        return Question("${answer.toOdia()} ପାଇଁ ସଠିକ ସମୀକରଣ ଖୋଜ", options, correctExpr.toOdiaNumerals())
    }
    private fun generateExpression(target: Int): Pair<String, Int> {
        val safeTarget = if (target <= 1) Random.nextInt(2, 20) else target
        val level = _gameState.value.selectedLevel
        while (true) {
            try {
                when (level) {
                    "ସହଜ" -> return if (Random.nextBoolean()) {
                        val n1 = Random.nextInt(1, safeTarget); "$n1 + ${safeTarget - n1}" to safeTarget
                    } else {
                        val n2 = Random.nextInt(1, 20); "${safeTarget + n2} - $n2" to safeTarget
                    }
                    "ମଧ୍ୟମ" -> return if (Random.nextBoolean()) {
                        val factors = findFactors(safeTarget).filter { it > 1 && it != safeTarget }
                        val n1 = if (factors.isNotEmpty()) factors.random() else 2
                        if (safeTarget % n1 == 0) "$n1 × ${safeTarget / n1}" to safeTarget else generateExpression(safeTarget)
                    } else {
                        val n2 = Random.nextInt(2, 11); "${safeTarget * n2} ÷ $n2" to safeTarget
                    }
                    else -> {
                        if (Random.nextBoolean()) {
                            val factors = findFactors(safeTarget).filter { it > 1 }
                            if (factors.isNotEmpty()) {
                                val c = factors.random(); val abSum = safeTarget / c
                                if (abSum * c == safeTarget && abSum > 2) {
                                    val a = Random.nextInt(1, abSum); val b = abSum - a
                                    return "($a + $b) × $c" to safeTarget
                                }
                            }
                        }
                        val n2 = Random.nextInt(2, 11); return "${safeTarget * n2} ÷ $n2" to safeTarget
                    }
                }
            } catch (e: Exception) { /* retry loop */ }
        }
    }
    private fun findFactors(number: Int): List<Int> = (1..abs(number)).filter { number % it == 0 }
    private fun generateOptions(answer: Int, count: Int = 4): List<Int> {
        val options = mutableSetOf(answer); val range = maxOf(1, answer / 10)
        while (options.size < count) {
            val offset = (Random.nextInt(-range - 2, range + 3).takeIf { it != 0 } ?: 1)
            val wrong = answer + offset
            if (wrong >= 0) { options.add(wrong) }
        }
        return options.toList().shuffled()
    }
}