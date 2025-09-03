// FILE: app/src/main/java/com/sandeep/ganitabigyan/DrawingViewModel.kt

package com.sandeep.ganitabigyan

import android.graphics.Bitmap
import android.os.Environment
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Path
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

// --- THE FIX IS HERE ---
// The enum now holds a reference to a String Resource ID instead of a hardcoded string.
enum class DrawingMode(@StringRes val displayNameResId: Int) {
    SERIAL(R.string.drawing_mode_serial),
    RANDOM_DOUBLE(R.string.drawing_mode_random_double),
    RANDOM_TRIPLE(R.string.drawing_mode_random_triple)
}

data class DrawingUiState(
    val currentNumber: Int = 1,
    val paths: List<Path> = emptyList(),
    val undonePaths: List<Path> = emptyList(),
    val currentMode: DrawingMode = DrawingMode.SERIAL,
    val serialCounter: Int = 1
)

class DrawingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    fun changeMode(newMode: DrawingMode) {
        _uiState.update {
            it.copy(
                currentMode = newMode,
                currentNumber = getNextNumber(newMode, 1),
                paths = emptyList(),
                undonePaths = emptyList(),
                serialCounter = 1
            )
        }
    }

    fun nextNumber() {
        _uiState.update { currentState ->
            val nextSerialCounter = if (currentState.currentMode == DrawingMode.SERIAL) {
                currentState.serialCounter + 1
            } else {
                currentState.serialCounter
            }
            currentState.copy(
                currentNumber = getNextNumber(currentState.currentMode, nextSerialCounter),
                serialCounter = nextSerialCounter,
                paths = emptyList(),
                undonePaths = emptyList()
            )
        }
    }

    private fun getNextNumber(mode: DrawingMode, serialCounter: Int): Int {
        return when (mode) {
            DrawingMode.SERIAL -> serialCounter
            DrawingMode.RANDOM_DOUBLE -> Random.nextInt(10, 100)
            DrawingMode.RANDOM_TRIPLE -> Random.nextInt(100, 1000)
        }
    }

    fun clearCanvas() {
        _uiState.update { it.copy(paths = emptyList(), undonePaths = emptyList()) }
    }

    fun addPath(path: Path) {
        _uiState.update { it.copy(paths = it.paths + path, undonePaths = emptyList()) }
    }

    fun undo() {
        if (_uiState.value.paths.isNotEmpty()) {
            _uiState.update {
                val lastPath = it.paths.last()
                it.copy(paths = it.paths.dropLast(1), undonePaths = listOf(lastPath) + it.undonePaths)
            }
        }
    }

    fun redo() {
        if (_uiState.value.undonePaths.isNotEmpty()) {
            _uiState.update {
                val pathToRedo = it.undonePaths.first()
                it.copy(paths = it.paths + pathToRedo, undonePaths = it.undonePaths.drop(1))
            }
        }
    }

    fun saveDrawing(bitmap: Bitmap?) {
        if (bitmap == null || _uiState.value.paths.isEmpty()) return
        val number = uiState.value.currentNumber
        val timestamp = System.currentTimeMillis()
        val fileName = "Drawing_${number}_$timestamp.png"
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appDir = File(picturesDir, "GanitaBigyan")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val file = File(appDir, fileName)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}