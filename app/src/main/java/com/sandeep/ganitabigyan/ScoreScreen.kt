package com.sandeep.ganitabigyan

import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sandeep.ganitabigyan.utils.toOdia
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var lifetimeScore by remember { mutableStateOf(Pair(0, 0)) } // Correct, Wrong

    LaunchedEffect(Unit) {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
        val file = File(ganitaBigyanDir, "lifetime_score.gba")
        if (file.exists()) {
            val parts = file.readText().split(",")
            if (parts.size == 2) {
                lifetimeScore = Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ଲାଇଫ୍‌ଟାଇମ୍ ସ୍କୋର") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ସମୁଦାୟ ଫଳାଫଳ", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ScoreCard("ଠିକ୍", lifetimeScore.first)
                ScoreCard("ଭୁଲ୍", lifetimeScore.second)
            }
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Text(score.toOdia(), style = MaterialTheme.typography.displayMedium)
        }
    }
}