package com.sandeep.ganitabigyan

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.ganitabigyan.utils.toOdia
import com.sandeep.ganitabigyan.utils.toOdiaNumerals
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onMenuClick: () -> Unit,
    onNavigateToScore: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { gameState.questions.size })
    val coroutineScope = rememberCoroutineScope()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSolutionDialog by remember { mutableStateOf(false) }

    val currentQuestion = gameState.questions.getOrNull(pagerState.currentPage)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowTimedChallengeDialog -> showSettingsDialog = true
                is UiEvent.RequestAutoScroll -> {
                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                viewModel.updateCurrentQuestionIndex(page)
            }
    }

    LaunchedEffect(Unit) {
        viewModel.hapticEvent.collect { event ->
            viewModel.triggerHapticFeedback(event)
        }
    }

    // BUG FIX: This ensures the pager scrolls to the top when settings change
    // and the question list is reset.
    LaunchedEffect(gameState.questions) {
        if (gameState.currentQuestionIndex == 0 && pagerState.currentPage != 0 && gameState.questions.isNotEmpty()) {
            coroutineScope.launch {
                pagerState.scrollToPage(0)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "background_transition")
    val backgroundAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "background_alpha"
    )
    val gradient = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * backgroundAlpha),
            MaterialTheme.colorScheme.background
        ),
        radius = 1200f
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (gameState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Scaffold(
                topBar = {
                    GameTopBar(
                        gameState = gameState,
                        onMenuClick = onMenuClick,
                        onStopChallengeClick = { viewModel.stopTimedChallenge() },
                        onScoreClick = onNavigateToScore
                    )
                },
                floatingActionButton = {
                    val showScrollButton by remember {
                        derivedStateOf {
                            val activeQuestion = gameState.questions.getOrNull(pagerState.currentPage)
                            pagerState.currentPage < gameState.currentQuestionIndex && activeQuestion?.isAnswered == true
                        }
                    }
                    AnimatedVisibility(
                        visible = showScrollButton,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        FloatingActionButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(gameState.currentQuestionIndex)
                            }
                        }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Scroll to current question")
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().background(gradient)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = { if (!gameState.isTimedChallenge) showSettingsDialog = true },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            enabled = !gameState.isTimedChallenge
                        ) {
                            Text("ପ୍ରକାର: ${gameState.selectedType} | ସ୍ତର: ${gameState.selectedLevel}")
                        }

                        VerticalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            userScrollEnabled = !gameState.isTimedChallenge && !gameState.isAutoScrollEnabled,
                            beyondBoundsPageCount = 3
                        ) { pageIndex ->
                            val question = gameState.questions.getOrNull(pageIndex)
                            if (question != null) {
                                key(question.questionText, question.isAnswered, question.userAnswer) {
                                    QuestionCard(
                                        question = question,
                                        onAnswer = { answer -> viewModel.onAnswerSelected(answer) }
                                    )
                                }
                            }
                        }

                        if (gameState.selectedType != "ସମୀକରଣ ଖୋଜ") {
                            Button(
                                onClick = { showSolutionDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("ସମାଧାନ", color = Color.White)
                            }
                        }
                        MotivationalFooter(quotes = viewModel.motivationalQuotes)
                    }
                    FeedbackBanner(message = gameState.feedbackMessage, modifier = Modifier.align(Alignment.TopCenter).padding(padding))
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            initialType = gameState.selectedType,
            initialLevel = gameState.selectedLevel,
            gameTypes = viewModel.gameTypes,
            difficultyLevels = viewModel.difficultyLevels,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { type, level ->
                viewModel.updateSettings(type, level)
                showSettingsDialog = false
            }
        )
    }

    if (gameState.challengeJustFinished) {
        ChallengeSummaryDialog(
            score = gameState.score,
            wrong = gameState.wrongAttempts,
            onPlayAgain = {
                val currentType = gameState.selectedType
                val currentLevel = gameState.selectedLevel
                viewModel.startTimedChallenge(5, currentType, currentLevel)
            },
            onExit = { viewModel.dismissChallengeSummary() }
        )
    }

    if (showSolutionDialog && currentQuestion?.solution != null) {
        SolutionDialog(
            solution = currentQuestion.solution,
            onDismiss = { showSolutionDialog = false }
        )
    }
}

@Composable
fun QuestionCard(
    question: Question,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(question.options) { option ->
                val isSelected = question.userAnswer == option
                val isCorrectAnswer = question.correctAnswer == option

                val targetColor = when {
                    question.isAnswered && isCorrectAnswer -> Color(0xFF4CAF50)
                    question.isAnswered && isSelected -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.surface
                }

                val buttonBackgroundColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(300),
                    label = "button_bg_color"
                )

                val buttonTextColor by animateColorAsState(
                    targetValue = if (targetColor != MaterialTheme.colorScheme.surface) Color.White else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(300),
                    label = "button_text_color"
                )

                OutlinedButton(
                    onClick = { if (!question.isAnswered) onAnswer(option) },
                    modifier = Modifier.height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = buttonBackgroundColor,
                        contentColor = buttonTextColor
                    ),
                    enabled = !question.isAnswered
                ) {
                    Text(text = option, fontSize = 24.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    gameState: GameState,
    onMenuClick: () -> Unit,
    onStopChallengeClick: () -> Unit,
    onScoreClick: () -> Unit
) {
    TopAppBar(
        title = { Text("ଗଣିତ ବିଜ୍ଞ") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            if (gameState.isTimedChallenge) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(gameState.timerValue / 60000).toInt().toOdia()}:${String.format("%02d", (gameState.timerValue % 60000 / 1000).toInt()).toOdiaNumerals()}",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    TextButton(onClick = onStopChallengeClick) {
                        Text("ବନ୍ଦ କରନ୍ତୁ")
                    }
                }
            } else {
                Text(
                    "ଠିକ୍: ${gameState.score.toOdia()} | ଭୁଲ୍: ${gameState.wrongAttempts.toOdia()}",
                    modifier = Modifier.clickable(onClick = onScoreClick).padding(16.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        modifier = Modifier.statusBarsPadding()
    )
}

@Composable
fun SettingsDialog(
    initialType: String,
    initialLevel: String,
    gameTypes: List<String>,
    difficultyLevels: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (type: String, level: String) -> Unit
) {
    var tempType by remember { mutableStateOf(initialType) }
    var tempLevel by remember { mutableStateOf(initialLevel) }

    val availableLevels by remember(tempType) {
        derivedStateOf {
            if (tempType in listOf("ମିଶ୍ରଣ", "ସମୀକରଣ ଖୋଜ")) {
                difficultyLevels
            } else {
                difficultyLevels.filter { it != "ଅତି କଠିନ" }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("ସେଟିଂସ ବଦଳାନ୍ତୁ", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.heightIn(max = 300.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ପ୍ରକାର", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        LazyColumn {
                            items(gameTypes) { type ->
                                val isSelected = type == tempType
                                Text(
                                    text = type,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable {
                                            tempType = type
                                            if (type !in listOf(
                                                    "ମିଶ୍ରଣ",
                                                    "ସମୀକରଣ ଖୋଜ"
                                                ) && tempLevel == "ଅତି କଠିନ"
                                            ) {
                                                tempLevel = "କଠିନ"
                                            }
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ସ୍ତର", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                        LazyColumn {
                            items(availableLevels) { level ->
                                val isSelected = level == tempLevel
                                Text(
                                    text = level,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .clickable { tempLevel = level }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ବାତିଲ କରନ୍ତୁ")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(tempType, tempLevel) }) {
                        Text("ଠିକ୍ ଅଛି")
                    }
                }
            }
        }
    }
}


@Composable
fun MotivationalFooter(quotes: List<String>) {
    var currentQuote by remember { mutableStateOf(quotes.random()) }
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(Random.nextLong(from = 5000, until = 10000))
            currentQuote = quotes.random()
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentQuote,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith (slideOutVertically { height -> -height } + fadeOut())
            }, label = "quote_animation"
        ) { quote ->
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChallengeSummaryDialog(score: Int, wrong: Int, onPlayAgain: () -> Unit, onExit: () -> Unit) {
    Dialog(onDismissRequest = {}) {
        Card(modifier = Modifier.wrapContentHeight(), shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("ସମୟ ସମାପ୍ତ!", style = MaterialTheme.typography.headlineSmall)
                Text("ଆପଣଙ୍କର ସ୍କୋର:", style = MaterialTheme.typography.titleLarge)
                Text("ଠିକ୍: ${score.toOdia()} | ଭୁଲ୍: ${wrong.toOdia()}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = onPlayAgain, modifier = Modifier.fillMaxWidth()) {
                        Text("ପୁଣି ଖେଳନ୍ତୁ")
                    }
                    OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                        Text("ଚ୍ୟାଲେଞ୍ଜ ବନ୍ଦ କରନ୍ତୁ")
                    }
                }
            }
        }
    }
}
@Composable
fun SolutionDialog(solution: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ସମାଧାନ") },
        text = { Text(solution, style = MaterialTheme.typography.headlineMedium) },
        confirmButton = { Button(onClick = onDismiss) { Text("ବନ୍ଦ କରନ୍ତୁ") } }
    )
}
@Composable
fun FeedbackBanner(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { -it - 50 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it - 50}) + fadeOut(),
        modifier = modifier.padding(top = 16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).shadow(4.dp, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                text = message ?: "",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}