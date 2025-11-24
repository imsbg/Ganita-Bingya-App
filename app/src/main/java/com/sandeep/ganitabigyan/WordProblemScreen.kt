// FILE: app/src/main/java/com/sandeep/ganitabigyan/WordProblemScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sandeep.ganitabigyan.utils.toLocaleNumerals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordProblemScreen(
    navController: NavController,
    viewModel: WordProblemViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current // <<< This is the correct, up-to-date context

    // Use 'remember' to avoid re-calculating on every recomposition unless the problem or context changes
    val (questionText, solutionText) = remember(uiState.currentProblem, context) {
        uiState.currentProblem?.resolve(context) ?: Pair("Loading...", "")
    }

    // ADDED: State to control the visibility of the digital slate
    var showSlate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.word_problem_game_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_description))
                    }
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            text = "${"✔:".toLocaleNumerals(context)} ${uiState.correctAnswers.toLocaleNumerals(context)}",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${"✖:".toLocaleNumerals(context)} ${uiState.wrongAnswers.toLocaleNumerals(context)}",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        // ADDED: Floating Action Button to open the slate
        floatingActionButton = {
            FloatingActionButton(onClick = { showSlate = true }) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.drawing_slate))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.currentProblem != null) {
                        AutoResizeText(
                            text = questionText, // Use the resolved text
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { viewModel.serveNextProblem() }) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = stringResource(id = R.string.skip_question),
                        maxLines = 1
                    )
                }
                // MODIFIED: This button now only shows the icon, as requested.
                Button(onClick = { viewModel.toggleSolution() }) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = stringResource(id = R.string.show_solution) // For accessibility
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnswerBox(userAnswer = uiState.userAnswer, isCorrect = uiState.isAnswerCorrect)
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = uiState.isAnswerCorrect != null,
                modifier = Modifier.height(48.dp),
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                val m = when (uiState.isAnswerCorrect) {
                    true -> R.string.correct_answer
                    false -> R.string.wrong_answer
                    else -> null
                }
                val c = if (uiState.isAnswerCorrect == true) Color(0xFF4CAF50) else Color(0xFFF44336)
                if (m != null) {
                    Text(text = stringResource(id = m), color = c, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }

            CustomKeypad(
                onNumberInput = { viewModel.onNumberInput(it) },
                onBackspace = { viewModel.onBackspace() },
                onSubmit = {
                    if (uiState.isAnswerCorrect == null) {
                        viewModel.checkAnswer(context)
                    } else {
                        viewModel.serveNextProblem()
                    }
                },
                isNextButton = uiState.isAnswerCorrect != null
            )
            if (uiState.showSolution) {
                AlertDialog(
                    onDismissRequest = { viewModel.toggleSolution() },
                    title = { Text(stringResource(id = R.string.solution)) },
                    text = { Text(solutionText) }, // <<< Use the resolved text
                    confirmButton = { TextButton(onClick = { viewModel.toggleSolution() }) { Text("OK") } }
                )
            }
        }
    }

    // ADDED: Conditionally display the DigitalSlate and pass the question text
    if (showSlate) {
        DigitalSlate(
            question = questionText,
            onDismiss = { showSlate = false }
        )
    }
}

@Composable
fun AutoResizeText(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified, textAlign: TextAlign? = null, style: TextStyle = LocalTextStyle.current,) {
    var scaledTextStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }
    Text(text = text, color = color, textAlign = textAlign, modifier = modifier.drawWithContent { if (readyToDraw) { drawContent() } }, onTextLayout = { textLayoutResult -> if (textLayoutResult.didOverflowHeight) { scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.95) } else { readyToDraw = true } }, style = scaledTextStyle, softWrap = true, maxLines = 10)
}

@Composable
fun AnswerBox(userAnswer: String, isCorrect: Boolean?) {
    val context = LocalContext.current
    val borderColor = when (isCorrect) {
        true -> Color(0xFF4CAF50)
        false -> Color(0xFFF44336)
        null -> MaterialTheme.colorScheme.primary
    }
    Box(modifier = Modifier.fillMaxWidth(0.8f).height(60.dp).border(2.dp, borderColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        Text(text = if (userAnswer.isEmpty()) "_" else userAnswer.toLocaleNumerals(context), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if (isCorrect == null) MaterialTheme.colorScheme.onSurface else borderColor)
    }
}

@Composable
fun CustomKeypad(onNumberInput: (String) -> Unit, onBackspace: () -> Unit, onSubmit: () -> Unit, isNextButton: Boolean) {
    val buttons = (1..9).map { it.toString() } + listOf("0")
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { number -> NumberButton(text = number, onClick = { onNumberInput(number) }) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(onClick = onBackspace, isSubmit = false)
            ActionButton(onClick = onSubmit, isSubmit = true, isNext = isNextButton)
        }
    }
}

@Composable
fun NumberButton(text: String, onClick: () -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = onClick, modifier = Modifier.size(64.dp), shape = CircleShape, contentPadding = PaddingValues(0.dp)) {
        Text(text.toLocaleNumerals(context), fontSize = 24.sp)
    }
}

@Composable
fun ActionButton(onClick: () -> Unit, isSubmit: Boolean, isNext: Boolean = false) {
    val colors = if (isSubmit) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
    val modifier = if (isSubmit) Modifier.width(136.dp).height(64.dp) else Modifier.size(64.dp)
    OutlinedButton(onClick = onClick, modifier = modifier, shape = if (isSubmit) RoundedCornerShape(32.dp) else CircleShape, colors = colors, contentPadding = PaddingValues(0.dp)) {
        when {
            isNext -> Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Next", modifier = Modifier.size(32.dp))
            isSubmit -> Icon(Icons.Default.Check, contentDescription = "Submit", modifier = Modifier.size(32.dp))
            else -> Icon(Icons.Default.Backspace, contentDescription = "Backspace")
        }
    }
}

// ADDED: The new DigitalSlate composable at the end of the file
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DigitalSlate(question: String, onDismiss: () -> Unit) {
    val completedPaths = remember { mutableStateListOf<Path>() }
    val undonePaths = remember { mutableStateListOf<Path>() }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val drawColor = MaterialTheme.colorScheme.onSurface
    val slateBackgroundColor = MaterialTheme.colorScheme.surface

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(id = R.string.drawing_slate)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close Slate")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (completedPaths.isNotEmpty()) {
                                    undonePaths.add(completedPaths.removeLast())
                                }
                            },
                            enabled = completedPaths.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                        }
                        IconButton(
                            onClick = {
                                if (undonePaths.isNotEmpty()) {
                                    completedPaths.add(undonePaths.removeLast())
                                }
                            },
                            enabled = undonePaths.isNotEmpty()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                        }
                        IconButton(
                            onClick = {
                                completedPaths.clear()
                                undonePaths.clear()
                            },
                            enabled = completedPaths.isNotEmpty()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Clear All")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                // ADDED: Box to display the question text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(max = 120.dp) // Limit height for very long text
                        .verticalScroll(rememberScrollState()), // Make it scrollable
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = question,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(slateBackgroundColor)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    undonePaths.clear()
                                    currentPathPoints = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPathPoints = currentPathPoints + change.position
                                },
                                onDragEnd = {
                                    if (currentPathPoints.size > 1) {
                                        val path = Path().apply {
                                            moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                                            currentPathPoints.drop(1).forEach { lineTo(it.x, it.y) }
                                        }
                                        completedPaths.add(path)
                                    }
                                    currentPathPoints = emptyList()
                                }
                            )
                        }
                ) {
                    completedPaths.forEach { path ->
                        drawPath(
                            path = path,
                            color = drawColor,
                            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    if (currentPathPoints.size > 1) {
                        val currentDrawPath = Path().apply {
                            moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                            currentPathPoints.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path = currentDrawPath,
                            color = drawColor,
                            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }
    }
}