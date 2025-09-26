// FILE: app/src/main/java/com/sandeep/ganitabigyan/ScoreScreen.kt

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
import com.sandeep.ganitabigyan.utils.toLocaleNumerals
import java.io.File

@Composable
fun ScoreContent() {
    val context = LocalContext.current
    var totalLifetimeScore by remember { mutableStateOf(Pair(0, 0)) } // Correct, Wrong

    LaunchedEffect(Unit) {
        var totalCorrect = 0
        var totalIncorrect = 0

        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")

        // --- Read Math Game Score ---
        try {
            val mathScoreFile = File(ganitaBigyanDir, "lifetime_score.gba")
            if (mathScoreFile.exists()) {
                val parts = mathScoreFile.readText().split(",")
                if (parts.size == 2) {
                    totalCorrect += parts[0].toIntOrNull() ?: 0
                    totalIncorrect += parts[1].toIntOrNull() ?: 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // --- Read Logic Game Score ---
        try {
            val logicScoreFile = File(ganitaBigyanDir, "logic_lifetime_score.gba")
            if (logicScoreFile.exists()) {
                val parts = logicScoreFile.readText().split(",")
                if (parts.size == 2) {
                    totalCorrect += parts[0].toIntOrNull() ?: 0
                    totalIncorrect += parts[1].toIntOrNull() ?: 0
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Update the UI with the combined total
        totalLifetimeScore = Pair(totalCorrect, totalIncorrect)
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
                score = totalLifetimeScore.first // Show total correct
            )
            ScoreCard(
                label = stringResource(R.string.score_incorrect),
                score = totalLifetimeScore.second // Show total incorrect
            )
        }
        // NOTE: The Share Score button will now share the combined total score.
        // No changes were needed for its code, as it captures the screen as-is.
    }
}

@Composable
fun ScoreCard(label: String, score: Int) {
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
            Text(score.toLocaleNumerals(context), style = MaterialTheme.typography.displayMedium)
        }
    }
}