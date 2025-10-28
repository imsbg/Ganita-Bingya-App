// FILE: app/src/main/java/com/sandeep/ganitabigyan/FTMNScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals

private data class FtmDisplayState(
    val sequence: List<Int?>,
    val options: List<Int>,
    val correctAnswer: Int,
    val explanation: String,
    val feedbackMessage: String?,
    val showFeedback: Boolean
)

private fun FTMNUiState.toDisplayedQuestion(context: android.content.Context): FtmDisplayState {
    val explanationText = if (this.solutionExplanationValue != null) { val localizedValue = this.solutionExplanationValue.toLocaleNumerals(context); context.getString(this.solutionExplanationResId, localizedValue) } else { context.getString(this.solutionExplanationResId) }; return FtmDisplayState(sequence = this.questionSequenceWithNull, options = this.answerOptions, correctAnswer = this.getCorrectAnswer(), explanation = explanationText, feedbackMessage = null, showFeedback = false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FTMNScreen(
    navController: NavController,
    viewModel: FTMNViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var displayState by remember { mutableStateOf(uiState.toDisplayedQuestion(context)) }

    LaunchedEffect(uiState) {
        if (displayState.sequence != uiState.questionSequenceWithNull) {
            displayState = uiState.toDisplayedQuestion(context)
        } else if (uiState.isAnswerChecked && !displayState.showFeedback) {
            val feedback = if (uiState.isAnswerCorrect) { context.getString(R.string.ftmn_feedback_correct) } else { context.getString(R.string.ftmn_feedback_incorrect, displayState.correctAnswer.toLocaleNumerals(context)) }; displayState = displayState.copy(feedbackMessage = feedback, showFeedback = true)
        }
    }

    // <<< ALL GAME OVER LOGIC IS REMOVED >>>

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.ftmn_title)) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description)) } }
            )
        }
    ) { paddingValues ->
        displayState.let { currentDisplay ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "${stringResource(id = R.string.ftmn_correct)}: ${uiState.correctCount.toLocaleNumerals(context)}", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center); VerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 16.dp)); Text(text = "${stringResource(id = R.string.ftmn_wrong)}: ${uiState.wrongCount.toLocaleNumerals(context)}", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentDisplay.sequence.forEach { number -> val displayText = number?.toLocaleNumerals(context) ?: "__"; Text(text = displayText, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        currentDisplay.options.chunked(2).forEach { rowOptions ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowOptions.forEach { option ->
                                    OptionButton(text = option.toLocaleNumerals(context), onClick = { viewModel.onOptionSelected(option) }, isSelected = uiState.selectedAnswer == option, isCorrect = currentDisplay.correctAnswer == option, isAnswerChecked = uiState.isAnswerChecked, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(visible = currentDisplay.showFeedback, enter = fadeIn(tween(500))) {
                        val feedbackColor = if (uiState.isAnswerCorrect) Color(0xFF00C853) else MaterialTheme.colorScheme.error; Text(text = currentDisplay.feedbackMessage ?: "", color = feedbackColor, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(8.dp))
                    AnimatedVisibility(visible = uiState.isSolutionVisible, enter = slideInVertically { -it } + fadeIn(), exit = slideOutVertically { -it } + fadeOut()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = stringResource(id = R.string.ftmn_solution_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp)); Text(text = currentDisplay.explanation, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    AnimatedVisibility(visible = currentDisplay.showFeedback, enter = slideInVertically { it / 2 } + fadeIn(), exit = fadeOut()) {
                        OutlinedButton(onClick = { viewModel.toggleSolutionVisibility() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text(text = if (uiState.isSolutionVisible) stringResource(id = R.string.ftmn_hide_solution) else stringResource(id = R.string.ftmn_show_solution))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { if (uiState.isAnswerChecked) viewModel.nextQuestion() else viewModel.checkAnswer(context) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = uiState.selectedAnswer != null
                ) {
                    Text(text = if (uiState.isAnswerChecked) stringResource(id = R.string.ftmn_next_button) else stringResource(id = R.string.ftmn_check_button))
                }
            }
        }
    }
}

@Composable
private fun OptionButton(text: String, onClick: () -> Unit, isSelected: Boolean, isCorrect: Boolean, isAnswerChecked: Boolean, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier.height(60.dp), shape = RoundedCornerShape(12.dp), enabled = !isAnswerChecked, colors = when {
        isAnswerChecked && isCorrect -> ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
        isAnswerChecked && isSelected && !isCorrect -> ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        isSelected -> ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else -> ButtonDefaults.outlinedButtonColors()
    }, border = when {
        isAnswerChecked && isCorrect -> BorderStroke(2.dp, Color.White)
        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        else -> ButtonDefaults.outlinedButtonBorder
    }) { Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}