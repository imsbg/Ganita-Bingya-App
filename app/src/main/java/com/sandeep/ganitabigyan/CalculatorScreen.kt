// FILE: app/src/main/java/com/sandeep/ganitabigyan/CalculatorScreen.kt
package com.sandeep.ganitabigyan

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

// This class is correct and needs no changes
class OdiaNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val odiaText = OdiaNumberUtil.toOdia(text.text)
        return TransformedText(
            text = AnnotatedString(odiaText),
            offsetMapping = OffsetMapping.Identity
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onNavigateBack: () -> Unit, viewModel: CalculatorViewModel = viewModel()) {
    // This composable is correct
    Surface(color = MaterialTheme.colorScheme.surface) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("କ୍ୟାଲକୁଲେଟର") },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                val displayWeight by animateFloatAsState(
                    targetValue = if (viewModel.isScientificPadVisible.value) 0.6f else 1f,
                    label = "displayWeight"
                )
                CalculatorDisplay(viewModel, Modifier.weight(displayWeight))
                CalculatorPad(viewModel)
            }
        }
    }
}

// FIXED: This version uses a readOnly text field and a manual tap gesture detector.
@Composable
fun ColumnScope.CalculatorDisplay(viewModel: CalculatorViewModel, modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            reverseLayout = true,
            horizontalAlignment = Alignment.End
        ) {
            item {
                SelectionContainer {
                    Column(horizontalAlignment = Alignment.End) {
                        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

                        BasicTextField(
                            value = viewModel.expression.value,
                            onValueChange = {}, // This does nothing because the field is read-only
                            readOnly = true,    // This PREVENTS the keyboard and system input
                            textStyle = TextStyle(
                                fontFamily = OdiaFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 48.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                            ),
                            onTextLayout = { result ->
                                // Capture the layout result to calculate tap positions
                                textLayoutResult = result
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    // This gesture detector handles taps manually
                                    detectTapGestures { offset ->
                                        textLayoutResult?.let { layoutResult ->
                                            // Calculate which character index corresponds to the tap coordinate
                                            val newCursorOffset = layoutResult.getOffsetForPosition(offset)
                                            // Tell the ViewModel to move the cursor
                                            viewModel.moveCursor(newCursorOffset)
                                        }
                                    }
                                },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            visualTransformation = OdiaNumberVisualTransformation(),
                        )

                        AnimatedVisibility(visible = viewModel.liveResult.value.isNotEmpty()) {
                            Text(
                                text = OdiaNumberUtil.toOdia(viewModel.liveResult.value),
                                fontFamily = OdiaFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 36.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End, maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            // History list remains unchanged and correct
            items(viewModel.history) { calc ->
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { viewModel.loadFromHistory(calc) }
                ) {
                    Text(
                        text = OdiaNumberUtil.toOdia(calc.expression),
                        fontFamily = OdiaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = OdiaNumberUtil.toOdia(calc.result),
                        fontFamily = OdiaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}


// The rest of the file is unchanged and correct.
@Composable
fun CalculatorPad(viewModel: CalculatorViewModel) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScientificPad(viewModel)
            NumberPad(viewModel)
        }
    }
}

@Composable
fun ScientificPad(viewModel: CalculatorViewModel) {
    AnimatedVisibility(
        visible = viewModel.isScientificPadVisible.value,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val buttonHeight = 52.dp
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val degText = OdiaNumberUtil.toOdia(if (viewModel.isDegMode.value) "DEG" else "RAD")
                val invText = OdiaNumberUtil.toOdia("INV")
                CalculatorButton(degText, Modifier.weight(1f).height(buttonHeight), isToggle = true) { viewModel.onAction(CalculatorAction.ToggleDeg) }
                CalculatorButton(invText, Modifier.weight(1f).height(buttonHeight), isToggle = viewModel.isInverse.value) { viewModel.onAction(CalculatorAction.ToggleInv) }
                CalculatorButton("e", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.E) }
                CalculatorButton("%", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("%")) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val sin = if (viewModel.isInverse.value) "sin⁻¹" else "sin"
                val cos = if (viewModel.isInverse.value) "cos⁻¹" else "cos"
                val tan = if (viewModel.isInverse.value) "tan⁻¹" else "tan"
                CalculatorButton(sin, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("sin")) }
                CalculatorButton(cos, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("cos")) }
                CalculatorButton(tan, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("tan")) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val ln = if (viewModel.isInverse.value) "2ˣ" else "ln"
                val log = if (viewModel.isInverse.value) "10ˣ" else "log"
                CalculatorButton(ln, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("ln")) }
                CalculatorButton(log, Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Scientific("log")) }
                CalculatorButton("!", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Factorial) }
            }
        }
    }
}

@Composable
fun NumberPad(viewModel: CalculatorViewModel) {
    val toggleIcon = if (viewModel.isScientificPadVisible.value) Icons.Default.ExpandMore else Icons.Default.KeyboardArrowUp

    val buttonHeight by animateDpAsState(
        targetValue = if (viewModel.isScientificPadVisible.value) 52.dp else 64.dp,
        label = "buttonHeightAnimation"
    )
    val buttonSpacing = 8.dp

    Column(verticalArrangement = Arrangement.spacedBy(buttonSpacing)) {
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton("AC", Modifier.weight(1f).height(buttonHeight), isClear = true) { viewModel.onAction(CalculatorAction.Clear) }
            CalculatorButton("()", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Parentheses) }
            CalculatorButton("del", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Delete) }
            IconButton(
                onClick = { viewModel.toggleScientificPad() },
                modifier = Modifier.weight(1f).height(buttonHeight)
            ) { Icon(toggleIcon, "Toggle scientific", tint = MaterialTheme.colorScheme.primary) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            "789".forEach { num -> CalculatorButton(OdiaNumberUtil.toOdia(num.toString()), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number(num.toString())) } }
            CalculatorButton("÷", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("÷")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            "456".forEach { num -> CalculatorButton(OdiaNumberUtil.toOdia(num.toString()), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number(num.toString())) } }
            CalculatorButton("×", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("×")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            "123".forEach { num -> CalculatorButton(OdiaNumberUtil.toOdia(num.toString()), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number(num.toString())) } }
            CalculatorButton("-", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("-")) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(buttonSpacing)) {
            CalculatorButton(OdiaNumberUtil.toOdia("0"), Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Number("0")) }
            CalculatorButton(".", Modifier.weight(1f).height(buttonHeight)) { viewModel.onAction(CalculatorAction.Decimal) }
            CalculatorButton("=", Modifier.weight(1f).height(buttonHeight), isEqual = true) { viewModel.onAction(CalculatorAction.Calculate) }
            CalculatorButton("+", Modifier.weight(1f).height(buttonHeight), isOperator = true) { viewModel.onAction(CalculatorAction.Operator("+")) }
        }
    }
}


@Composable
fun RowScope.CalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    isOperator: Boolean = false, isEqual: Boolean = false, isClear: Boolean = false, isToggle: Boolean = false,
    onClick: () -> Unit
) {
    val colors = when {
        isEqual -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        isClear -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
        isOperator || isToggle -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        else -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    val shape = CircleShape

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        colors = colors,
        contentPadding = PaddingValues(0.dp)
    ) {
        if (symbol == "del") {
            Icon(Icons.Default.Backspace, "Delete")
        } else {
            Text(symbol, fontFamily = OdiaFontFamily, fontSize = 20.sp, textAlign = TextAlign.Center)
        }
    }
}