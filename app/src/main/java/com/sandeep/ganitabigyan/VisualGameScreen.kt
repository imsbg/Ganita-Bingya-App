// FILE: app/src/main/java/com/sandeep/ganitabigyan/VisualGameScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualGameScreen(
    navController: NavController,
    viewModel: VisualGameViewModel = viewModel()
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
                title = { Text(stringResource(id = R.string.visual_game_title)) },
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
            onAnswerSelected = { selectedGroup -> viewModel.onAnswerSelected(selectedGroup) }
        )
    }
}

@Composable
private fun GameContent(
    modifier: Modifier = Modifier,
    state: VisualGameState,
    onAnswerSelected: (FruitGroup) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBarContent(
            questionNumber = state.questionNumber,
            score = state.score
        )

        Text(
            text = stringResource(id = state.currentQuestion.textResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val correctAnswer = if (state.currentQuestion == QuestionType.MORE) {
                state.fruitGroups.maxByOrNull { it.quantity }
            } else {
                state.fruitGroups.minByOrNull { it.quantity }
            }

            state.fruitGroups.forEach { fruitGroup ->
                FruitGroupCard(
                    modifier = Modifier.weight(1f),
                    fruitGroup = fruitGroup,
                    isAnswered = state.isAnswered,
                    correctAnswer = correctAnswer,
                    onClick = { onAnswerSelected(fruitGroup) }
                )
            }
        }

        val feedbackText = state.feedbackResId?.let { resId ->
            stringResource(id = resId, formatArgs = state.feedbackArgs.toTypedArray())
        } ?: ""

        AnimatedContent(
            targetState = feedbackText,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "Feedback Text"
        ) { text ->
            Text(
                text = text.toLocaleNumerals(context),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (state.feedbackResId == R.string.feedback_correct) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.height(48.dp)
            )
        }
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
            text = stringResource(R.string.visual_game_progress, questionNumber).toLocaleNumerals(context),
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

// <<< FIX: Added the missing @Composable annotation here >>>
@Composable
private fun FruitGroupCard(
    modifier: Modifier = Modifier,
    fruitGroup: FruitGroup,
    isAnswered: Boolean,
    correctAnswer: FruitGroup?,
    onClick: () -> Unit
) {
    val isCorrect = fruitGroup == correctAnswer
    val borderColor by animateColorAsState(targetValue = when { !isAnswered -> Color.Transparent; isCorrect -> Color(0xFF4CAF50); else -> Color.Gray }, animationSpec = tween(300), label = "borderColor")
    val alpha by animateFloatAsState(targetValue = when { !isAnswered -> 1.0f; isCorrect -> 1.0f; else -> 0.5f }, animationSpec = tween(300), label = "alpha")
    val scale by animateFloatAsState(targetValue = if (isAnswered && isCorrect) 1.05f else 1.0f, animationSpec = tween(300), label = "scale")

    Card(
        modifier = modifier
            .aspectRatio(0.7f)
            .scale(scale)
            .alpha(alpha)
            .border(4.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = !isAnswered, onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        FruitDisplay(fruitGroup = fruitGroup)
    }
}

@Composable
private fun FruitDisplay(fruitGroup: FruitGroup) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val imageSize = when {
            fruitGroup.quantity > 7 -> maxWidth / 3.5f
            fruitGroup.quantity > 4 -> maxWidth / 3.0f
            else -> maxWidth / 2.5f
        }.coerceAtMost(40.dp)

        val offsets = remember(fruitGroup.quantity, fruitGroup.type) {
            List(fruitGroup.quantity) {
                getFruitOffset(maxWidth - imageSize, maxHeight - imageSize)
            }
        }

        for (i in 0 until fruitGroup.quantity) {
            Image(
                painter = painterResource(id = fruitGroup.type.imageResId),
                contentDescription = fruitGroup.type.name,
                modifier = Modifier
                    .size(imageSize)
                    .offset(x = offsets[i].x, y = offsets[i].y),
                contentScale = ContentScale.Fit
            )
        }
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

private data class DpOffset(val x: Dp, val y: Dp)

private fun getFruitOffset(availableWidth: Dp, availableHeight: Dp): DpOffset {
    val randomX = (Random.nextFloat() - 0.5f) * 0.7f
    val randomY = (Random.nextFloat() - 0.5f) * 0.7f
    val offsetX = availableWidth * randomX
    val offsetY = availableHeight * randomY
    return DpOffset(x = offsetX, y = offsetY)
}