// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE
// FILE: app/src/main/java/com/sandeep/ganitabigyan/GameScreen.kt

package com.sandeep.ganitabigyan

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class GameOption(val key: String, val displayName: String)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameScreen(viewModel: GameViewModel, onNavigateBack: () -> Unit, onNavigateToScore: () -> Unit) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { gameState.questions.size })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // <<< We will pass this context to the ViewModel
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSolutionDialog by remember { mutableStateOf(false) }
    val currentQuestion = gameState.questions.getOrNull(pagerState.currentPage)
    val isViewingPreviousQuestion by remember { derivedStateOf { gameState.questions.isNotEmpty() && gameState.currentQuestionIndex < pagerState.currentPage } }

    LaunchedEffect(Unit) { viewModel.soundEvent.collectLatest { event -> when (event) { is SoundEvent.CorrectAnswer -> playSound(context, R.raw.correct) } } }
    LaunchedEffect(Unit) { viewModel.resetGame() }
    LaunchedEffect(Unit) { viewModel.uiEvent.collectLatest { event -> when (event) { is UiEvent.ShowTimedChallengeDialog -> showSettingsDialog = true; is UiEvent.RequestAutoScroll -> if (pagerState.currentPage < pagerState.pageCount - 1) coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } } } }
    LaunchedEffect(pagerState) { snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page -> viewModel.updateCurrentQuestionIndex(page, context) } } // Pass context
    LaunchedEffect(Unit) { viewModel.hapticEvent.collect { event -> viewModel.triggerHapticFeedback(event) } }
    LaunchedEffect(gameState.questions) { if (gameState.currentQuestionIndex == 0 && pagerState.currentPage != 0 && gameState.questions.isNotEmpty()) coroutineScope.launch { pagerState.scrollToPage(0) } }

    val infiniteTransition = rememberInfiniteTransition(label = "background_transition"); val backgroundAlpha by infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(2000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "background_alpha"); val gradient = Brush.radialGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * backgroundAlpha), MaterialTheme.colorScheme.background), radius = 1200f)

    Box(modifier = Modifier.fillMaxSize()) {
        if (gameState.isLoading) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) } else {
            Scaffold(topBar = { GameTopBar(gameState = gameState, onNavigateBack = onNavigateBack, onStopChallengeClick = { viewModel.stopTimedChallenge(context) }, onScoreClick = onNavigateToScore) }) { padding ->
                Box(modifier = Modifier.fillMaxSize().background(gradient)) {
                    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val typeDisplay = getDisplayNameForKey(key = gameState.selectedTypeKey, context = context)
                        val levelDisplay = getDisplayNameForKey(key = gameState.selectedLevelKey, context = context)
                        OutlinedButton(onClick = { if (!gameState.isTimedChallenge) showSettingsDialog = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), enabled = !gameState.isTimedChallenge) { Text(stringResource(R.string.game_settings_button_text, typeDisplay, levelDisplay)) }
                        VerticalPager(state = pagerState, modifier = Modifier.weight(1f), userScrollEnabled = !gameState.isTimedChallenge, beyondBoundsPageCount = 3) { pageIndex ->
                            val question = gameState.questions.getOrNull(pageIndex)
                            if (question != null) { key(question.questionText, question.isAnswered, question.userAnswer) { QuestionCard(question = question, onAnswer = { answer -> viewModel.onAnswerSelected(answer, context) }) } } // Pass context
                        }
                        if (isViewingPreviousQuestion) { Button(onClick = { coroutineScope.launch { pagerState.animateScrollToPage(gameState.currentQuestionIndex) } }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),) { Text(stringResource(R.string.game_go_to_current_question)) } } else {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SwapVert, contentDescription = stringResource(R.string.game_auto_scroll_icon_desc)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.game_auto_scroll)); Spacer(Modifier.width(16.dp)); Switch(checked = gameState.isAutoScrollEnabled, onCheckedChange = { viewModel.toggleAutoScroll(it) })
                            }
                            val findEquationGameTypeKey = "game_type_find_equation"
                            if (gameState.selectedTypeKey != findEquationGameTypeKey) {
                                Button(onClick = { showSolutionDialog = true }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text(stringResource(R.string.game_show_solution), color = Color.White) }
                            }
                        }
                        MotivationalFooter()
                    }
                    FeedbackBanner(message = gameState.feedbackMessage, modifier = Modifier.align(Alignment.TopCenter).padding(padding))
                }
            }
        }
    }
    if (showSettingsDialog) { SettingsDialog(initialTypeKey = gameState.selectedTypeKey, initialLevelKey = gameState.selectedLevelKey, onDismiss = { showSettingsDialog = false }, onConfirm = { typeKey, levelKey -> viewModel.updateSettings(typeKey, levelKey, context); showSettingsDialog = false }) } // Pass context
    if (gameState.challengeJustFinished) { ChallengeSummaryDialog(score = gameState.score, wrong = gameState.wrongAttempts, onPlayAgain = { val currentTypeKey = gameState.selectedTypeKey; val currentLevelKey = gameState.selectedLevelKey; viewModel.startTimedChallenge(5, currentTypeKey, currentLevelKey, context) }, onExit = { viewModel.dismissChallengeSummary(context) }) } // Pass context
    if (showSolutionDialog && currentQuestion != null) {
        LaunchedEffect(currentQuestion) {
            viewModel.onAnswerSelected(currentQuestion.userAnswer ?: context.getString(R.string.qna_log_skipped_hint), context) // Pass context
        }
        SolutionDialog(solution = currentQuestion.solution?.toLocaleNumerals(context) ?: "", onDismiss = { showSolutionDialog = false })
    }
}

// Pass context to this helper function as well
@Composable private fun getDisplayNameForKey(key: String, context: Context): String {
    val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
    return if (resourceId != 0) context.getString(resourceId) else key
}

// ... All other Composables (playSound, QuestionCard, SettingsDialog etc.) are unchanged ...
private fun playSound(context: Context, soundResId: Int) { val mediaPlayer = MediaPlayer.create(context, soundResId); mediaPlayer.setOnCompletionListener { mp -> mp.release() }; mediaPlayer.start() }
@Composable fun QuestionCard(question: Question, onAnswer: (String) -> Unit) { val context = LocalContext.current; Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(text = question.questionText.toLocaleNumerals(context), style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp), textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 24.dp)); LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(question.options) { option -> val isSelected = question.userAnswer == option; val isCorrectAnswer = question.correctAnswer == option; val targetBackgroundColor = when { question.isAnswered && isCorrectAnswer -> Color(0xFFC8E6C9); question.isAnswered && isSelected -> Color(0xFFFFCDD2); else -> MaterialTheme.colorScheme.surface }; val targetBorderColor = when { question.isAnswered && isCorrectAnswer -> Color(0xFF388E3C); question.isAnswered && isSelected -> Color(0xFFD32F2F); else -> MaterialTheme.colorScheme.outline }; val animatedBackgroundColor by animateColorAsState(targetValue = targetBackgroundColor, animationSpec = tween(300), label = ""); val animatedBorderColor by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300), label = ""); val animatedTextColor by animateColorAsState(targetValue = if (question.isAnswered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface, animationSpec = tween(300), label = ""); OutlinedButton(onClick = { if (!question.isAnswered) onAnswer(option) }, modifier = Modifier.height(100.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(2.dp, animatedBorderColor), colors = ButtonDefaults.outlinedButtonColors(containerColor = animatedBackgroundColor, contentColor = animatedTextColor), enabled = !question.isAnswered) { Text(text = option.toLocaleNumerals(context), fontSize = 24.sp, textAlign = TextAlign.Center) } } } } }
@Composable fun SettingsDialog(initialTypeKey: String, initialLevelKey: String, onDismiss: () -> Unit, onConfirm: (typeKey: String, levelKey: String) -> Unit) { var tempTypeKey by remember { mutableStateOf(initialTypeKey) }; var tempLevelKey by remember { mutableStateOf(initialLevelKey) }; val gameTypes = listOf(GameOption("game_type_addition", stringResource(R.string.game_type_addition)), GameOption("game_type_subtraction", stringResource(R.string.game_type_subtraction)), GameOption("game_type_multiplication", stringResource(R.string.game_type_multiplication)), GameOption("game_type_division", stringResource(R.string.game_type_division)), GameOption("game_type_mixed", stringResource(R.string.game_type_mixed)), GameOption("game_type_find_equation", stringResource(R.string.game_type_find_equation))); val difficultyLevels = listOf(GameOption("difficulty_easy", stringResource(R.string.difficulty_easy)), GameOption("difficulty_medium", stringResource(R.string.difficulty_medium)), GameOption("difficulty_hard", stringResource(R.string.difficulty_hard)), GameOption("difficulty_very_hard", stringResource(R.string.difficulty_very_hard))); val availableLevels by remember(tempTypeKey) { derivedStateOf { if (tempTypeKey in listOf("game_type_mixed", "game_type_find_equation")) { difficultyLevels } else { difficultyLevels.filter { it.key != "difficulty_very_hard" } } } }; Dialog(onDismissRequest = onDismiss) { Card(shape = RoundedCornerShape(16.dp)) { Column(modifier = Modifier.padding(24.dp)) { Text(stringResource(R.string.game_settings_dialog_title), style = MaterialTheme.typography.headlineSmall); Spacer(modifier = Modifier.height(16.dp)); Row(modifier = Modifier.heightIn(max = 300.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { Column(modifier = Modifier.weight(1f)) { Text(stringResource(R.string.game_settings_dialog_type), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)); LazyColumn { items(gameTypes) { option -> val isSelected = option.key == tempTypeKey; Text(text = option.displayName, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent).clickable { tempTypeKey = option.key; if (option.key !in listOf("game_type_mixed", "game_type_find_equation") && tempLevelKey == "difficulty_very_hard") { tempLevelKey = "difficulty_hard" } }.padding(8.dp)) } } }; Column(modifier = Modifier.weight(1f)) { Text(stringResource(R.string.game_settings_dialog_level), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)); LazyColumn { items(availableLevels) { option -> val isSelected = option.key == tempLevelKey; Text(text = option.displayName, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent).clickable { tempLevelKey = option.key }.padding(8.dp)) } } } }; Spacer(modifier = Modifier.height(24.dp)); Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(onClick = onDismiss) { Text(stringResource(R.string.game_settings_dialog_cancel)) }; Spacer(modifier = Modifier.width(8.dp)); Button(onClick = { onConfirm(tempTypeKey, tempLevelKey) }) { Text(stringResource(R.string.ok_button)) } } } } } }
@OptIn(ExperimentalMaterial3Api::class) @Composable fun GameTopBar(gameState: GameState, onNavigateBack: () -> Unit, onStopChallengeClick: () -> Unit, onScoreClick: () -> Unit) { val context = LocalContext.current; TopAppBar(title = { }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_description)) } }, actions = { if (gameState.isTimedChallenge) { Row(verticalAlignment = Alignment.CenterVertically) { Text(text = "${(gameState.timerValue / 60000).toInt().toLocaleNumerals(context)}:${String.format("%02d", (gameState.timerValue % 60000 / 1000).toInt()).toLocaleNumerals(context)}", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp)); TextButton(onClick = onStopChallengeClick) { Text(stringResource(R.string.game_top_bar_stop)) } } } else { Text(stringResource(R.string.game_top_bar_score_info, gameState.score.toLocaleNumerals(context), gameState.wrongAttempts.toLocaleNumerals(context)), modifier = Modifier.clickable(onClick = onScoreClick).padding(16.dp)) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)), modifier = Modifier.statusBarsPadding()) }
@Composable fun MotivationalFooter() { val quotes = stringArrayResource(R.array.motivational_quotes); var currentQuote by remember { mutableStateOf(quotes.random()) }; LaunchedEffect(key1 = Unit) { while (true) { delay(Random.nextLong(from = 5000, until = 10000)); currentQuote = quotes.random() } }; Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) { AnimatedContent(targetState = currentQuote, transitionSpec = { (slideInVertically { height -> height } + fadeIn()) togetherWith (slideOutVertically { height -> -height } + fadeOut()) }, label = "quote_animation") { quote -> Text(text = quote, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center) } } }
@Composable fun ChallengeSummaryDialog(score: Int, wrong: Int, onPlayAgain: () -> Unit, onExit: () -> Unit) { val context = LocalContext.current; Dialog(onDismissRequest = {}) { Card(modifier = Modifier.wrapContentHeight(), shape = RoundedCornerShape(16.dp)) { Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) { Text(stringResource(R.string.game_summary_dialog_title), style = MaterialTheme.typography.headlineSmall); Text(stringResource(R.string.game_summary_dialog_your_score), style = MaterialTheme.typography.titleLarge); Text(stringResource(R.string.game_top_bar_score_info, score.toLocaleNumerals(context), wrong.toLocaleNumerals(context)), style = MaterialTheme.typography.titleMedium); Spacer(modifier = Modifier.height(16.dp)); Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.game_summary_dialog_play_again)) }; OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.game_summary_dialog_stop_challenge)) } } } } } }
@Composable fun SolutionDialog(solution: String, onDismiss: () -> Unit) { AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.game_solution_dialog_title)) }, text = { Text(solution, style = MaterialTheme.typography.headlineMedium) }, confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.game_solution_dialog_close)) } }) }
@Composable fun FeedbackBanner(message: String?, modifier: Modifier = Modifier) { AnimatedVisibility(visible = message != null, enter = slideInVertically { -it - 50 } + fadeIn(), exit = slideOutVertically { -it - 50 } + fadeOut(), modifier = modifier.padding(top = 16.dp)) { Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).shadow(4.dp, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Text(text = message ?: "", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = MaterialTheme.colorScheme.onSecondaryContainer, textAlign = TextAlign.Center) } } }