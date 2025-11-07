// PASTE THIS ENTIRE, NEW CODE INTO YOUR FILE

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
import androidx.compose.ui.text.style.TextOverflow
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

        fun readScoreFile(fileName: String): Pair<Int, Int> {
            try {
                val file = File(ganitaBigyanDir, fileName)
                if (file.exists()) {
                    val parts = file.readText().split(",")
                    if (parts.size == 2) {
                        return Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            return Pair(0, 0)
        }

        val mathScore = readScoreFile("lifetime_score.gba")
        val logicScore = readScoreFile("logic_lifetime_score.gba")
        val ftmnScore = readScoreFile("ftmn_lifetime_score.gba")
        val wordProblemScore = readScoreFile("word_problem_score.gba")

        totalCorrect = mathScore.first + logicScore.first + ftmnScore.first + wordProblemScore.first
        totalIncorrect = mathScore.second + logicScore.second + ftmnScore.second + wordProblemScore.second

        totalLifetimeScore = Pair(totalCorrect, totalIncorrect)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.score_total_results),
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(32.dp))

        // --- KEY CHANGE 1: Make the Row responsive ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScoreCard(
                label = stringResource(R.string.score_correct),
                score = totalLifetimeScore.first,
                // Give each card equal weight to fill the space
                modifier = Modifier.weight(1f)
            )
            ScoreCard(
                label = stringResource(R.string.score_incorrect),
                score = totalLifetimeScore.second,
                // Give each card equal weight to fill the space
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ScoreCard(label: String, score: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier, // Use the modifier passed from the Row
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            // --- KEY CHANGE 2: Adjust padding to give text more room ---
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                // --- KEY CHANGE 3: Ensure text stays on a single line ---
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = score.toLocaleNumerals(context),
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}