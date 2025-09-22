// FILE: app/src/main/java/com/sandeep/ganitabigyan/LogicGameScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogicGameScreen(
    navController: NavController,
    viewModel: LogicGameViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val milestone = state.milestoneReached
    if (milestone != null) {
        MilestoneDialog(
            milestone = milestone,
            onDismiss = { viewModel.onMilestoneAcknowledged() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.logic_game_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                }
            )
        }
    ) { paddingValues ->
        GameContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onAnswerSelected = { selectedNumber -> viewModel.onAnswerSelected(selectedNumber) }
        )
    }
}

@Composable
private fun GameContent(
    modifier: Modifier = Modifier,
    state: LogicGameState,
    onAnswerSelected: (Int) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopBarContent(
            questionNumber = state.questionNumber,
            score = state.score
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = state.questionType.textResId),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (state.questionText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.questionText.toLocaleNumerals(context),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        AnswerGrid(
            options = state.answerOptions,
            isAnswered = state.isAnswered,
            correctAnswer = state.correctAnswer,
            onAnswerSelected = onAnswerSelected
        )

        // <<< FIX: This Text now uses the new resource ID from the ViewModel >>>
        val feedbackText = state.feedbackResId?.let { resId ->
            // This builds the string with its arguments (like the correct number)
            stringResource(id = resId, formatArgs = state.feedbackArgs.toTypedArray())
        } ?: ""

        Text(
            text = feedbackText.toLocaleNumerals(context),
            style = MaterialTheme.typography.bodyLarge, // <<< CHANGE: Use a smaller style
            fontWeight = FontWeight.Bold, // <<< OPTIONAL: Make it bold to stand out
            textAlign = TextAlign.Center, // <<< NEW: Center the text
            color = if (state.feedbackResId == R.string.feedback_correct_n) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
            modifier = Modifier.height(48.dp)
        )
    }
}

@Composable
private fun TopBarContent(questionNumber: Int, score: Int) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.logic_game_progress, questionNumber).toLocaleNumerals(context),
            style = MaterialTheme.typography.bodyLarge
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Score", tint = Color(0xFFFFC107))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = score.toLocaleNumerals(context),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AnswerGrid(
    options: List<Int>,
    isAnswered: Boolean,
    correctAnswer: Int,
    onAnswerSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (options.isNotEmpty()) AnswerButton(Modifier.weight(1f), options[0], isAnswered, options[0] == correctAnswer) { onAnswerSelected(options[0]) }
            if (options.size > 1) AnswerButton(Modifier.weight(1f), options[1], isAnswered, options[1] == correctAnswer) { onAnswerSelected(options[1]) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (options.size > 2) AnswerButton(Modifier.weight(1f), options[2], isAnswered, options[2] == correctAnswer) { onAnswerSelected(options[2]) }
            if (options.size > 3) AnswerButton(Modifier.weight(1f), options[3], isAnswered, options[3] == correctAnswer) { onAnswerSelected(options[3]) }
        }
    }
}

@Composable
private fun AnswerButton(
    modifier: Modifier = Modifier,
    number: Int,
    isAnswered: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val buttonColors by animateColorAsState(targetValue = when { !isAnswered -> MaterialTheme.colorScheme.primaryContainer; isCorrect -> Color(0xFFC8E6C9); else -> Color(0xFFFFCDD2) }, animationSpec = tween(300), label = "buttonColor")
    val textColor by animateColorAsState(targetValue = when { !isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer; isCorrect -> Color(0xFF2E7D32); else -> Color(0xFFC62828) }, animationSpec = tween(300), label = "textColor")
    val alpha by animateFloatAsState(targetValue = if (isAnswered && !isCorrect) 0.5f else 1.0f, animationSpec = tween(300), label = "alpha")

    Button(
        onClick = onClick,
        enabled = !isAnswered,
        modifier = modifier
            .height(100.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = buttonColors)
    ) {
        Text(text = number.toLocaleNumerals(context), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun MilestoneDialog(milestone: Int, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.milestone_title)) },
        text = { Text(stringResource(id = R.string.milestone_message, milestone).toLocaleNumerals(context)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.ok_button))
            }
        }
    )
}