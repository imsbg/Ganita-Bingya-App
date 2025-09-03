// FILE: app/src/main/java/com/sandeep/ganitabigyan/DrawingHistoryScreen.kt
// VERSION: FINAL - Fully multilingual

package com.sandeep.ganitabigyan

import android.graphics.BitmapFactory
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// <<< CHANGE 1: Import the new, correct function >>>
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File

data class SavedDrawing(val file: File, val number: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingHistoryScreen(onNavigateBack: () -> Unit) {
    var savedDrawings by remember { mutableStateOf<List<SavedDrawing>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // <<< CHANGE 2: Get the context >>>
    val context = LocalContext.current

    fun loadDrawings() {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val ganitaBigyanDir = File(picturesDir, "GanitaBigyan")
        if (ganitaBigyanDir.exists()) {
            savedDrawings = ganitaBigyanDir.listFiles()
                ?.filter { it.name.startsWith("Drawing_") && it.extension == "png" }
                ?.mapNotNull { file ->
                    val number = file.nameWithoutExtension.split("_").getOrNull(1)?.toIntOrNull()
                    if (number != null) SavedDrawing(file, number) else null
                }
                ?.sortedByDescending { it.file.lastModified() } ?: emptyList()
        } else {
            savedDrawings = emptyList()
        }
    }

    LaunchedEffect(Unit) {
        loadDrawings()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.drawing_history_delete_dialog_title)) },
            text = { Text(stringResource(R.string.drawing_history_delete_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val ganitaBigyanDir = File(picturesDir, "GanitaBigyan")
                        if (ganitaBigyanDir.exists()) {
                            ganitaBigyanDir.deleteRecursively()
                        }
                        savedDrawings = emptyList()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.drawing_history_delete_confirm)) }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.drawing_history_delete_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.drawing_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                actions = {
                    if (savedDrawings.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = stringResource(R.string.drawing_history_delete_all))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (savedDrawings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.drawing_history_empty))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(paddingValues).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedDrawings) { drawing ->
                    Card(elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val bitmap = BitmapFactory.decodeFile(drawing.file.absolutePath)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = stringResource(R.string.drawing_history_saved_drawing_desc),
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                // <<< CHANGE 3: Use the new multilingual function >>>
                                text = stringResource(R.string.drawing_history_question_label, drawing.number.toLocaleNumerals(context)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}