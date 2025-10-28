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

        // Helper function to read a score file
        fun readScoreFile(fileName: String): Pair<Int, Int> {
            try {
                val file = File(ganitaBigyanDir, fileName)
                if (file.exists()) {
                    val parts = file.readText().split(",")
                    if (parts.size == 2) {
                        return Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Pair(0, 0)
        }

        val mathScore = readScoreFile("lifetime_score.gba")
        val logicScore = readScoreFile("logic_lifetime_score.gba")
        // <<< NEW: READ FTMN SCORE >>>
        val ftmnScore = readScoreFile("ftmn_lifetime_score.gba")

        totalCorrect = mathScore.first + logicScore.first + ftmnScore.first
        totalIncorrect = mathScore.second + logicScore.second + ftmnScore.second

        totalLifetimeScore = Pair(totalCorrect, totalIncorrect)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.score_total_results), style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            ScoreCard(label = stringResource(R.string.score_correct), score = totalLifetimeScore.first)
            ScoreCard(label = stringResource(R.string.score_incorrect), score = totalLifetimeScore.second)
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int) {
    val context = LocalContext.current
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Text(score.toLocaleNumerals(context), style = MaterialTheme.typography.displayMedium)
        }
    }
}