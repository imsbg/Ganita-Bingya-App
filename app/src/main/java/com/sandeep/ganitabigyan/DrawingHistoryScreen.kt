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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toOdia
import java.io.File

data class SavedDrawing(val file: File, val number: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingHistoryScreen(onNavigateBack: () -> Unit) {
    var savedDrawings by remember { mutableStateOf<List<SavedDrawing>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            title = { Text("ସବୁ ଡିଲିଟ୍ କରନ୍ତୁ?") },
            text = { Text("ଆପଣ ନିଶ୍ଚିତ କି ଆପଣ ସମସ୍ତ ସେଭ୍ ହୋଇଥିବା ଡ୍ରଇଂ ଡିଲିଟ୍ କରିବାକୁ ଚାହୁଁଛନ୍ତି?") },
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
                ) { Text("ହଁ, ଡିଲିଟ୍ କରନ୍ତୁ") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("ନା") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ଡ୍ରଇଂ ଇତିହାସ") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    if (savedDrawings.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.DeleteForever, contentDescription = "Delete All") }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (savedDrawings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("କୌଣସି ଡ୍ରଇଂ ସେଭ୍ ହୋଇନାହିଁ")
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
                                contentDescription = "Saved Drawing",
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "ପ୍ରଶ୍ନ: ${drawing.number.toOdia()}",
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