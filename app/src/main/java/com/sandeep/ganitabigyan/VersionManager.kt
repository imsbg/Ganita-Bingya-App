// ସୁରକ୍ଷା ମୁଟାବକ ଡାଟା ସଫା କରିବା ପାଇଁ ବ୍ୟବହାର
// FILE: app/src/main/java/com/sandeep/ganitabigyan/VersionManager.kt

package com.sandeep.ganitabigyan // ଗଣିତ ବିଜ୍ଞ

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.File

// The compiler will find BuildConfig automatically.

private val Context.versionDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_version")

object VersionManager {

    private val LAST_RUN_VERSION_CODE = intPreferencesKey("last_run_version_code")
    private const val VERSION_10_0_0_CODE = 1000

    suspend fun handleUpdate(context: Context) {
        val dataStore = context.versionDataStore

        // This will now work without any special import
        val currentVersionCode = BuildConfig.VERSION_CODE

        val lastRunVersionCode = dataStore.data.first()[LAST_RUN_VERSION_CODE] ?: 0

        if (currentVersionCode >= VERSION_10_0_0_CODE && lastRunVersionCode < VERSION_10_0_0_CODE) {
            clearAllHistoryAndScoreFiles(context)
            dataStore.edit { preferences ->
                preferences[LAST_RUN_VERSION_CODE] = currentVersionCode
            }
        } else if (currentVersionCode > lastRunVersionCode) {
            dataStore.edit { preferences ->
                preferences[LAST_RUN_VERSION_CODE] = currentVersionCode
            }
        }
    }

    private fun clearAllHistoryAndScoreFiles(context: Context) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val ganitaBigyanDir = File(documentsDir, "GanitaBigyan")

            if (ganitaBigyanDir.exists()) {
                val filesToDelete = listOf(
                    "qna.gba", "logic_history.gba", "ftmn_history.gba", "word_problem_history.gba",
                    "lifetime_score.gba", "logic_lifetime_score.gba", "ftmn_lifetime_score.gba",
                    "word_problem_score.gba", "drawing_history.json"
                )
                filesToDelete.forEach { fileName ->
                    val file = File(ganitaBigyanDir, fileName)
                    if (file.exists()) file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}