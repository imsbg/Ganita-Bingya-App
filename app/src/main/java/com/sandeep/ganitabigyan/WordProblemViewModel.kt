// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE
// FILE: app/src/main/java/com/sandeep/ganitabigyan/WordProblemViewModel.kt

package com.sandeep.ganitabigyan

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class WordProblem(val questionResId: Int, val solutionResId: Int, val answer: Int, val numbers: List<Int>, val subTotal: String = "")
data class ProblemTemplate(val questionResId: Int, val solutionResId: Int, val generator: () -> Triple<List<Int>, Int, String>)
data class WordProblemUiState(val currentProblem: WordProblem? = null, val userAnswer: String = "", val isAnswerCorrect: Boolean? = null, val showSolution: Boolean = false, val correctAnswers: Int = 0, val wrongAnswers: Int = 0, val hintUsed: Boolean = false)

class WordProblemViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(WordProblemUiState())
    val uiState: StateFlow<WordProblemUiState> = _uiState.asStateFlow()
    private var questionTemplates: MutableList<ProblemTemplate> = mutableListOf()

    init {
        loadTemplates()
        serveNextProblem()
    }

    private fun saveHistory(isCorrect: Boolean, context: Context) {
        viewModelScope.launch {
            try {
                val problem = _uiState.value.currentProblem ?: return@launch
                val ganitaBigyanDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "GanitaBigyan").apply { mkdirs() }
                val file = File(ganitaBigyanDir, "word_problem_history.gba")

                val langCode = context.resources.configuration.locales[0].language
                val questionLog = "resId:${problem.questionResId}|numbers:${problem.numbers.joinToString(",")}|lang:$langCode"

                val logEntry = buildString {
                    appendLine("[${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())}]")
                    appendLine("${context.getString(R.string.wp_log_question_key)}: $questionLog")
                    appendLine("${context.getString(R.string.wp_log_your_answer_key)}: ${_uiState.value.userAnswer.ifEmpty { "N/A" }}")
                    appendLine("${context.getString(R.string.wp_log_correct_answer_key)}: ${problem.answer}")
                    val resultValue = if (isCorrect) context.getString(R.string.qna_log_result_correct_id) else context.getString(R.string.qna_log_result_incorrect_id)
                    appendLine("${context.getString(R.string.wp_log_result_key)}: $resultValue")
                    appendLine("${context.getString(R.string.wp_log_hint_used_key)}: ${_uiState.value.hintUsed}")
                    appendLine()
                }
                file.appendText(logEntry)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun checkAnswer(context: Context) {
        // FIX: If the answer has already been checked for this problem, do nothing.
        // This prevents saving duplicate history entries if the check button is tapped multiple times.
        if (_uiState.value.isAnswerCorrect != null) return

        val problem = _uiState.value.currentProblem ?: return
        val userAnswerInt = _uiState.value.userAnswer.toIntOrNull()
        val isCorrect = userAnswerInt == problem.answer
        if (isCorrect) {
            _uiState.update { it.copy(isAnswerCorrect = true, correctAnswers = it.correctAnswers + 1) }
            updateScore(1, 0)
        } else {
            _uiState.update { it.copy(isAnswerCorrect = false, wrongAnswers = it.wrongAnswers + 1) }
            updateScore(0, 1)
        }
        saveHistory(isCorrect, context)
    }

    fun serveNextProblem() { if (questionTemplates.isEmpty()) loadTemplates(); val template = questionTemplates.removeAt(0); val (numbers, answer, subTotal) = template.generator(); _uiState.update { it.copy(currentProblem = WordProblem(template.questionResId, template.solutionResId, answer, numbers, subTotal), userAnswer = "", isAnswerCorrect = null, showSolution = false, hintUsed = false) } }
    private fun loadTemplates() { val allTemplates = listOf(ProblemTemplate(R.string.q_add_balloons,R.string.s_add_balloons){val n1=Random.nextInt(5,15);val n2=Random.nextInt(5,15);Triple(listOf(n1,n2),n1+n2,"")},ProblemTemplate(R.string.q_add_birds,R.string.s_add_birds){val n1=Random.nextInt(10,25);val n2=Random.nextInt(10,25);Triple(listOf(n1,n2),n1+n2,"")},ProblemTemplate(R.string.q_add_fish,R.string.s_add_fish){val n1=Random.nextInt(15,30);val n2=Random.nextInt(15,30);Triple(listOf(n1,n2),n1+n2,"")},ProblemTemplate(R.string.q_add_money,R.string.s_add_money){val n1=Random.nextInt(20,70);val n2=Random.nextInt(10,30);Triple(listOf(n1,n2),n1+n2,"")},ProblemTemplate(R.string.q_add_children,R.string.s_add_children){val n1=Random.nextInt(20,40);val n2=Random.nextInt(20,40);Triple(listOf(n1,n2),n1+n2,"")},ProblemTemplate(R.string.q_sub_flowers,R.string.s_sub_flowers){val n1=Random.nextInt(50,99);val n2=Random.nextInt(10,n1-10);Triple(listOf(n1,n2),n1-n2,"")},ProblemTemplate(R.string.q_sub_pencils,R.string.s_sub_pencils){val n1=Random.nextInt(30,80);val n2=Random.nextInt(10,n1-10);Triple(listOf(n1,n2),n1-n2,"")},ProblemTemplate(R.string.q_sub_birds,R.string.s_sub_birds){val n1=Random.nextInt(40,90);val n2=Random.nextInt(10,n1-10);Triple(listOf(n1,n2),n1-n2,"")},ProblemTemplate(R.string.q_sub_money,R.string.s_sub_money){val n1=Random.nextInt(50,99);val n2=Random.nextInt(10,n1-10);Triple(listOf(n1,n2),n1-n2,"")},ProblemTemplate(R.string.q_sub_pages,R.string.s_sub_pages){val n1=Random.nextInt(50,99);val n2=Random.nextInt(10,n1-10);Triple(listOf(n1,n2),n1-n2,"")},ProblemTemplate(R.string.q_mul_dozen,R.string.s_mul_dozen){val n1=12;val n2=Random.nextInt(2,9);Triple(listOf(n1,n2),n1*n2,"")},ProblemTemplate(R.string.q_mul_pencils,R.string.s_mul_pencils){val n1=Random.nextInt(2,9);val n2=Random.nextInt(2,9);Triple(listOf(n1,n2),n1*n2,"")},ProblemTemplate(R.string.q_mul_pages,R.string.s_mul_pages){val n1=Random.nextInt(2,9);val n2=Random.nextInt(2,9);Triple(listOf(n1,n2),n1*n2,"")},ProblemTemplate(R.string.q_mul_wheels,R.string.s_mul_wheels){val n1=4;val n2=Random.nextInt(2,9);Triple(listOf(n1,n2),n1*n2,"")},ProblemTemplate(R.string.q_mul_price,R.string.s_mul_price){val n1=Random.nextInt(5,15);val n2=Random.nextInt(2,9);Triple(listOf(n1,n2),n1*n2,"")},ProblemTemplate(R.string.q_div_mangoes,R.string.s_div_mangoes){val n2=Random.nextInt(2,9);val ans=Random.nextInt(2,9);val n1=n2*ans;Triple(listOf(n1,n2),ans,"")},ProblemTemplate(R.string.q_div_toffee,R.string.s_div_toffee){val n2=Random.nextInt(2,9);val ans=Random.nextInt(5,15);val n1=n2*ans;Triple(listOf(n1,n2),ans,"")},ProblemTemplate(R.string.q_div_bananas,R.string.s_div_bananas){val n2=Random.nextInt(2,9);val ans=Random.nextInt(2,9);val n1=n2*ans;Triple(listOf(n1,n2),ans,"")},ProblemTemplate(R.string.q_div_weeks,R.string.s_div_weeks){val n2=7;val ans=Random.nextInt(2,9);val n1=n2*ans;Triple(listOf(n1,n2),ans,"")},ProblemTemplate(R.string.q_mix_mul_add,R.string.s_mix_mul_add){val n1=Random.nextInt(2,9);val n2=Random.nextInt(2,9);val n3=Random.nextInt(2,9);val s=n1*n2;Triple(listOf(n1,n2,n3),s+n3,s.toString())},ProblemTemplate(R.string.q_mix_mul_sub,R.string.s_mix_mul_sub){val n1=Random.nextInt(10,20);val n2=Random.nextInt(2,5);val n3=n1*n2+Random.nextInt(10,50);val s=n1*n2;Triple(listOf(n3,n1,n2),n3-s,s.toString())},ProblemTemplate(R.string.q_mix_add_sub,R.string.s_mix_add_sub){val n1=Random.nextInt(10,40);val n2=Random.nextInt(5,30);val s=n1+n2;val n3=(s/50+1)*50;Triple(listOf(n1,n2,n3),n3-s,s.toString())});questionTemplates = allTemplates.shuffled().toMutableList()}
    fun toggleSolution() { _uiState.update { it.copy(showSolution = !it.showSolution, hintUsed = true) } }
    fun onNumberInput(digit: String) { if (_uiState.value.userAnswer.length < 6) { _uiState.update { it.copy(userAnswer = it.userAnswer + digit) } } }
    fun onBackspace() { if (_uiState.value.userAnswer.isNotEmpty()) { _uiState.update { it.copy(userAnswer = it.userAnswer.dropLast(1)) } } }
    private fun updateScore(correctIncrement: Int, incorrectIncrement: Int) { viewModelScope.launch { try { val context = getApplication<Application>().applicationContext; val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS); val ganitaBigyanDir = File(documentsDir, "GanitaBigyan"); if (!ganitaBigyanDir.exists()) ganitaBigyanDir.mkdirs(); val file = File(ganitaBigyanDir, "word_problem_score.gba"); var currentCorrect = 0; var currentIncorrect = 0; if (file.exists()) { val parts = file.readText().split(","); if (parts.size == 2) { currentCorrect = parts[0].toIntOrNull() ?: 0; currentIncorrect = parts[1].toIntOrNull() ?: 0 } }; file.writeText("${currentCorrect + correctIncrement},${currentIncorrect + incorrectIncrement}") } catch (e: Exception) { e.printStackTrace() } } }
}

fun WordProblem.resolve(context: Context): Pair<String, String> { var resolvedQuestion = context.getString(this.questionResId); var resolvedSolution = context.getString(this.solutionResId); this.numbers.forEachIndexed { index, num -> val placeholder = "{${index + 1}}"; val localizedNum = num.toLocaleNumerals(context); resolvedQuestion = resolvedQuestion.replace(placeholder, localizedNum); resolvedSolution = resolvedSolution.replace(placeholder, localizedNum) }; if (this.subTotal.isNotBlank()) { resolvedSolution = resolvedSolution.replace("{sub_total}", this.subTotal.toLocaleNumerals(context)) }; resolvedSolution = resolvedSolution.replace("{ans}", this.answer.toLocaleNumerals(context)); return Pair(resolvedQuestion, resolvedSolution) }