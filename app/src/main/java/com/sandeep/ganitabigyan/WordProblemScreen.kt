// FILE: app/src/main/java/com/sandeep/ganitabigyan/WordProblemScreen.kt

package com.sandeep.ganitabigyan

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val context = LocalContext.current

    val problem = uiState.currentProblem?.resolve(context)

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
                    if (problem != null) {
                        AutoResizeText(
                            text = problem.questionText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                OutlinedButton(onClick = { viewModel.serveNextProblem() }) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(id = R.string.skip_question))
                }
                Button(onClick = { viewModel.toggleSolution() }) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(id = R.string.show_solution))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AnswerBox(userAnswer = uiState.userAnswer, isCorrect = uiState.isAnswerCorrect)
            Spacer(modifier = Modifier.height(16.dp))

            // <<< START OF THE FIX >>>
            AnimatedVisibility(
                visible = uiState.isAnswerCorrect != null,
                modifier = Modifier.height(48.dp),
                // Add these two lines for the slide and fade animation
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                // <<< END OF THE FIX >>>
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
                        viewModel.checkAnswer()
                    } else {
                        viewModel.serveNextProblem()
                    }
                },
                isNextButton = uiState.isAnswerCorrect != null
            )
            if (uiState.showSolution && problem != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.toggleSolution() },
                    title = { Text(stringResource(id = R.string.solution)) },
                    text = { Text(problem.solutionText) },
                    confirmButton = { TextButton(onClick = { viewModel.toggleSolution() }) { Text("OK") } }
                )
            }
        }
    }
}

// All other composables are unchanged and correct
@Composable
fun AutoResizeText(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified, textAlign: TextAlign? = null, style: TextStyle = LocalTextStyle.current,) { var scaledTextStyle by remember { mutableStateOf(style) }; var readyToDraw by remember { mutableStateOf(false) }; Text(text = text, color = color, textAlign = textAlign, modifier = modifier.drawWithContent { if (readyToDraw) { drawContent() } }, onTextLayout = { textLayoutResult -> if (textLayoutResult.didOverflowHeight) { scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.95) } else { readyToDraw = true } }, style = scaledTextStyle, softWrap = true, maxLines = 10) }
@Composable fun AnswerBox(userAnswer:String,isCorrect:Boolean?){val c=LocalContext.current;val b=when(isCorrect){true->Color(0xFF4CAF50);false->Color(0xFFF44336);null->MaterialTheme.colorScheme.primary};Box(modifier=Modifier.fillMaxWidth(0.8f).height(60.dp).border(2.dp,b,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Text(text=if(userAnswer.isEmpty())"_" else userAnswer.toLocaleNumerals(c),fontSize=32.sp,fontWeight=FontWeight.Bold,color=if(isCorrect==null)MaterialTheme.colorScheme.onSurface else b)}}
@Composable fun CustomKeypad(onNumberInput:(String)->Unit,onBackspace:()->Unit,onSubmit:()->Unit,isNextButton:Boolean){val b=(1..9).map{it.toString()}+listOf("0");Column(modifier=Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){b.chunked(3).forEach{r->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){r.forEach{n->NumberButton(text=n,onClick={onNumberInput(n)})}}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){ActionButton(onClick=onBackspace,isSubmit=false);ActionButton(onClick=onSubmit,isSubmit=true,isNext=isNextButton)}}}
@Composable fun NumberButton(text:String,onClick:()->Unit){val c=LocalContext.current;OutlinedButton(onClick=onClick,modifier=Modifier.size(64.dp),shape=CircleShape,contentPadding=PaddingValues(0.dp)){Text(text.toLocaleNumerals(c),fontSize=24.sp)}}
@Composable fun ActionButton(onClick:()->Unit,isSubmit:Boolean,isNext:Boolean=false){val c=if(isSubmit)ButtonDefaults.buttonColors()else ButtonDefaults.outlinedButtonColors();val m=if(isSubmit)Modifier.width(136.dp).height(64.dp)else Modifier.size(64.dp);OutlinedButton(onClick=onClick,modifier=m,shape=if(isSubmit)RoundedCornerShape(32.dp)else CircleShape,colors=c,contentPadding=PaddingValues(0.dp)){when{isNext->Icon(Icons.AutoMirrored.Filled.Redo,contentDescription="Next",modifier=Modifier.size(32.dp));isSubmit->Icon(Icons.Default.Check,contentDescription="Submit",modifier=Modifier.size(32.dp));else->Icon(Icons.Default.Backspace,contentDescription="Backspace")}}}