// FILE: app/src/main/java/com/sandeep/ganitabigyan/ChangelogScreen.kt
// VERSION: FINAL - With a powerful parser that handles any section header.

package com.sandeep.ganitabigyan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

// <<< NEW DATA CLASSES to support dynamic sections >>>
data class Changelog(val sections: List<ChangelogReleaseSection>)
data class ChangelogReleaseSection(val date: String, val version: String, val description: String, val subsections: List<ChangelogSubsection>)
data class ChangelogSubsection(val title: String, val items: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(onNavigateBack: () -> Unit) {
    var changelogState by remember { mutableStateOf<Changelog?>(null) }
    var changelogError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fetchAndParseChangelog(scope) { result, error ->
            changelogState = result
            changelogError = error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.changelog_dialog_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                changelogError -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.changelog_error)) }
                changelogState == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { ChangelogHeader() }
                        items(changelogState!!.sections) { section ->
                            ChangelogSectionCard(section = section)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangelogHeader() {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.changelog_header_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.changelog_header_description), style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ChangelogSectionCard(section: ChangelogReleaseSection) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(section.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(section.version, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))

            if (section.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(section.description, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
            }

            // Dynamically display all subsections
            section.subsections.forEach { subsection ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(subsection.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                subsection.items.forEach { item ->
                    ListItem(text = item)
                }
            }
        }
    }
}

@Composable
private fun ListItem(text: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.Top) {
        Text("•", modifier = Modifier.padding(horizontal = 8.dp), style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
    }
}


// <<< THIS IS THE FINAL, CORRECTED PARSER >>>
private fun fetchAndParseChangelog(scope: CoroutineScope, onResult: (Changelog?, Boolean) -> Unit) {
    scope.launch(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/imsbg/Ganita-Bingya-App/releases")
            val connection = url.openConnection() as java.net.HttpURLConnection
            val responseText = connection.inputStream.bufferedReader().readText()
            val releaseArray = JSONArray(responseText)
            val allReleaseSections = mutableListOf<ChangelogReleaseSection>()

            for (i in 0 until releaseArray.length()) {
                val releaseJson = releaseArray.getJSONObject(i)
                val body = releaseJson.getString("body")
                val version = releaseJson.getString("name")
                val dateString = releaseJson.getString("published_at")

                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                val date = outputFormat.format(inputFormat.parse(dateString)!!)

                val lines = body.lines()
                var description = ""
                val subsections = mutableListOf<ChangelogSubsection>()
                var currentSubsection: ChangelogSubsection? = null

                var descriptionDone = false

                for (line in lines) {
                    val trimmedLine = line.trim()
                        .replace("**", "") // Remove bold markdown

                    if (trimmedLine.startsWith("### ")) {
                        // If we have a current subsection, add it to the list before starting a new one
                        currentSubsection?.let { subsections.add(it) }
                        // Start a new subsection
                        val title = trimmedLine.removePrefix("### ").trim()
                        currentSubsection = ChangelogSubsection(title, mutableListOf())
                        descriptionDone = true
                    } else if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                        val item = trimmedLine.substring(2).trim()
                        if (item.isNotBlank()) {
                            // Add the item to the current subsection's list
                            (currentSubsection?.items as? MutableList)?.add(item)
                        }
                    } else if (trimmedLine.isNotBlank() && !descriptionDone) {
                        // This is part of the main description
                        description += trimmedLine + "\n"
                    }
                }
                // Add the last processed subsection
                currentSubsection?.let { subsections.add(it) }

                allReleaseSections.add(ChangelogReleaseSection(date, version, description.trim(), subsections))
            }
            withContext(Dispatchers.Main) { onResult(Changelog(allReleaseSections), false) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onResult(null, true) }
        }
    }
}