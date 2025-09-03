// FILE: app/src/main/java/com/sandeep/ganitabigyan/ScoreScreen.kt
// VERSION: FINAL - Uses the correct multilingual number converter.

package com.sandeep.ganitabigyan

import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
// <<< CHANGE 1: Import the new, correct function >>>
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File

@Composable
fun ScoreContent() {
    val context = LocalContext.current
    var lifetimeScore by remember { mutableStateOf(Pair(0, 0)) } // Correct, Wrong

    LaunchedEffect(Unit) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")
            val file = File(ganitaBigyanDir, "lifetime_score.gba")
            if (file.exists()) {
                val parts = file.readText().split(",")
                if (parts.size == 2) {
                    lifetimeScore = Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.score_total_results),
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ScoreCard(
                label = stringResource(R.string.score_correct),
                score = lifetimeScore.first
            )
            ScoreCard(
                label = stringResource(R.string.score_incorrect),
                score = lifetimeScore.second
            )
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int) {
    // <<< CHANGE 2: Get the context to pass to the converter function >>>
    val context = LocalContext.current
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            // <<< CHANGE 3: Use the new multilingual function instead of the old toOdia() >>>
            Text(score.toLocaleNumerals(context), style = MaterialTheme.typography.displayMedium)
        }
    }
}