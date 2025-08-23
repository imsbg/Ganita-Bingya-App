package com.sandeep.ganitabigyan

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sandeep.ganitabigyan.utils.toOdia
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    drawingViewModel: DrawingViewModel = viewModel()
) {
    val uiState by drawingViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var currentPath by remember { mutableStateOf(Path()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ଅଙ୍କନ ପ୍ୟାଡ୍") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = onNavigateToHistory) { Icon(Icons.Default.History, contentDescription = "ଇତିହାସ") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExposedDropdownMenuBox(expanded = isDropdownExpanded, onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }) {
                OutlinedTextField(
                    value = uiState.currentMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ମୋଡ୍ ବାଛନ୍ତୁ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = isDropdownExpanded, onDismissRequest = { isDropdownExpanded = false }) {
                    DrawingMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName) },
                            onClick = {
                                drawingViewModel.changeMode(mode)
                                currentPath = Path()
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiState.currentNumber.toOdia(),
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .onSizeChanged { size -> canvasSize = size }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) } },
                                    onDrag = { change, _ ->
                                        val newPath = Path().apply { addPath(currentPath); lineTo(change.position.x, change.position.y) }
                                        currentPath = newPath
                                    },
                                    onDragEnd = {
                                        drawingViewModel.addPath(currentPath)
                                        currentPath = Path()
                                    }
                                )
                            }
                    ) {
                        val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        uiState.paths.forEach { path -> drawPath(path = path, color = Color.Black, style = stroke) }
                        drawPath(path = currentPath, color = Color.Black, style = stroke)
                    }

                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { drawingViewModel.undo() }, enabled = uiState.paths.isNotEmpty()) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo")
                        }
                        OutlinedButton(onClick = { drawingViewModel.redo() }, enabled = uiState.undonePaths.isNotEmpty()) {
                            Icon(Icons.Default.Redo, contentDescription = "Redo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    drawingViewModel.clearCanvas()
                    currentPath = Path()
                }) { Text("ସଫା କରନ୍ତୁ") }
                Button(onClick = {
                    scope.launch {
                        if (canvasSize != IntSize.Zero) {
                            val bitmap = Bitmap.createBitmap(canvasSize.width, canvasSize.height, Bitmap.Config.ARGB_8888)
                            val canvas = AndroidCanvas(bitmap)
                            canvas.drawColor(android.graphics.Color.WHITE)
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                style = android.graphics.Paint.Style.STROKE
                                strokeWidth = 24f
                                strokeCap = android.graphics.Paint.Cap.ROUND
                                strokeJoin = android.graphics.Paint.Join.ROUND
                            }
                            uiState.paths.forEach { path -> canvas.drawPath(path.asAndroidPath(), paint) }
                            drawingViewModel.saveDrawing(bitmap)
                        }
                        drawingViewModel.nextNumber()
                        currentPath = Path()
                    }
                }) { Text("ପରବର୍ତ୍ତୀ") }
            }
        }
    }
}