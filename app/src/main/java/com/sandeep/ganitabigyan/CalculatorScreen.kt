// FILE: app/src/main/java/com/sandeep/ganitabigyan/CalculatorScreen.kt

package com.sandeep.ganitabigyan

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sandeep.ganitabigyan.ui.theme.OdiaFontFamily
import com.sandeep.ganitabigyan.utils.toLocaleNumerals


// <<< THIS IS THE MAIN FIX for the crash >>>
class LocaleAwareNumberVisualTransformation(private val context: Context) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val localizedText = originalText.toLocaleNumerals(context)

        // Create a smart OffsetMapping that handles changes in text length.
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // This is the critical fix.
                // It ensures the cursor position never goes out of bounds of the new, shorter text.
                return offset.coerceIn(0, localizedText.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // This handles the reverse mapping, e.g., for text selection.
                // A simple clamp is the safest way to prevent crashes here too.
                return offset.coerceIn(0, originalText.length)
            }
        }

        return TransformedText(
            text = AnnotatedString(localizedText),
            offsetMapping = offsetMapping // Use our new, safe mapping
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onNavigateBack: () -> Unit, viewModel: CalculatorViewModel = viewModel()) {
    val context = LocalContext.current
    Surface(color = MaterialTheme.colorScheme.surface) {
        Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.calculator_title)) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_description)) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }, containerColor = Color.Transparent) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                val displayWeight by animateFloatAsState(targetValue = if (viewModel.isScientificPadVisible.value) 0.6f else 1f, label = "displayWeight")
                CalculatorDisplay(viewModel, Modifier.weight(displayWeight), context)
                CalculatorPad(viewModel, context)
            }
        }
    }
}

@Composable
fun ColumnScope.CalculatorDisplay(viewModel: CalculatorViewModel, modifier: Modifier, context: Context) {
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), reverseLayout = true, horizontalAlignment = Alignment.End) {
            item {
                SelectionContainer {
                    Column(horizontalAlignment = Alignment.End) {
                        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                        BasicTextField(
                            value = viewModel.expression.value,
                            onValueChange = {},
                            readOnly = true,
                            // <<< MODIFICATION START: Dynamically set text color based on error state >>>
                            textStyle = TextStyle(
                                fontFamily = OdiaFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 48.sp,
                                color = if (viewModel.isError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            ),
                            // <<< MODIFICATION END >>>
                            onTextLayout = { result -> textLayoutResult = result },
                            modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectTapGestures { offset -> textLayoutResult?.let { layoutResult -> val newCursorOffset = layoutResult.getOffsetForPosition(offset); viewModel.moveCursor(newCursorOffset) } } },
                            // <<< MODIFICATION START: Also change cursor color for consistency >>>
                            cursorBrush = SolidColor(if (viewModel.isError.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary),
                            // <<< MODIFICATION END >>>
                            visualTransformation = LocaleAwareNumberVisualTransformation(context),
                        )
                        AnimatedVisibility(visible = viewModel.liveResult.value.isNotEmpty()) {
                            Text(text = viewModel.liveResult.value.toLocaleNumerals(context), fontFamily = OdiaFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.fillMaxWidth())
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            items(viewModel.history) { calc ->
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { viewModel.loadFromHistory(calc) }) {
                    Text(text = calc.expression.toLocaleNumerals(context), fontFamily = OdiaFontFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = calc.result.toLocaleNumerals(context), fontFamily = OdiaFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun CalculatorPad(viewModel: CalculatorViewModel, context: Context) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ScientificPad(viewModel)
            NumberPad(viewModel, context)
        }
    }
}

@Composable
fun ScientificPad(viewModel: CalculatorViewModel) {
    AnimatedVisibility(visible = viewModel.isScientificPadVisible.value, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        val buttonHeight = 52.dp
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val degText = if (viewModel.isDegMode.value) "DEG" else "RAD"; CalculatorButton(degText, Modifier.weight(1f).height(buttonHeight), isToggle = true) { viewModel.onAction(CalculatorAction.ToggleDeg) }; CalculatorButton("INV", Modifier.weight(1f).height(buttonHeight), isToggle = viewModel.isInverse.value) { viewModel.onAction(CalculatorAction.ToggleInv) }; CalculatorButton("π", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Pi) }; CalculatorButton("e", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.E) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val sin = if (viewModel.isInverse.value) "sin⁻¹" else "sin"; val cos = if (viewModel.isInverse.value) "cos⁻¹" else "cos"; val tan = if (viewModel.isInverse.value) "tan⁻¹" else "tan"; CalculatorButton(sin, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("sin")) }; CalculatorButton(cos, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("cos")) }; CalculatorButton(tan, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("tan")) }; CalculatorButton("%", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("%")) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ln = if (viewModel.isInverse.value) "2ˣ" else "ln"; val log = if (viewModel.isInverse.value) "10ˣ" else "log"; CalculatorButton(ln, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("ln")) }; CalculatorButton(log, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("log")) }; CalculatorButton("!", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Factorial) }; CalculatorButton("xʸ", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Power) }
            }
        }
    }
}

@Composable
fun NumberPad(viewModel: CalculatorViewModel, context: Context) {
    val toggleIcon = if (viewModel.isScientificPadVisible.value) Icons.Default.ExpandMore else Icons.Default.KeyboardArrowUp
    val buttonHeight by animateDpAsState(targetValue = if (viewModel.isScientificPadVisible.value) 52.dp else 64.dp, label = "buttonHeightAnimation")
    val buttonSpacing = 8.dp
    Column(verticalArrangement = Arrangement.spacedBy(buttonSpacing)) {
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(stringResource(R.string.calculator_clear_button), Modifier.weight(1f).height(buttonHeight), isClear = true) { viewModel.onAction(CalculatorAction.Clear) }; CalculatorButton("()", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Parentheses) }; CalculatorButton(stringResource(R.string.calculator_backspace_icon), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Delete) }; IconButton(onClick = { viewModel.toggleScientificPad() }, modifier = Modifier.weight(1f).height(buttonHeight)) { Icon(toggleIcon, stringResource(R.string.calculator_toggle_scientific_pad), tint = MaterialTheme.colorScheme.primary) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            "789".forEach { num -> CalculatorButton(num.toString().toLocaleNumerals(context), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number(num.toString())) } }; CalculatorButton("÷", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("÷")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            "456".forEach { num -> CalculatorButton(num.toString().toLocaleNumerals(context), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number(num.toString())) } }; CalculatorButton("×", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("×")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            "123".forEach { num -> CalculatorButton(num.toString().toLocaleNumerals(context), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number(num.toString())) } }; CalculatorButton("-", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("-")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("0".toLocaleNumerals(context), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number("0")) }; CalculatorButton(".", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Decimal) }; CalculatorButton("=", Modifier.weight(1f).height(buttonHeight), isEqual = true) { viewModel.onAction(CalculatorAction.Calculate) }; CalculatorButton("+", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("+")) }
        }
    }
}

@Composable
fun RowScope.CalculatorButton(symbol: String, modifier: Modifier = Modifier, isOperator: Boolean = false, isEqual: Boolean = false, isClear: Boolean = false, isToggle: Boolean = false, onClick: () -> Unit) {
    val colors = when { isEqual -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary); isClear -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer); isOperator || isToggle -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer); else -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant) }
    val shape = CircleShape
    Button(onClick = onClick, modifier = modifier, shape = shape, colors = colors, contentPadding = PaddingValues(0.dp)) { Text(symbol, fontFamily = OdiaFontFamily, fontSize = 20.sp, textAlign = TextAlign.Center) }
}