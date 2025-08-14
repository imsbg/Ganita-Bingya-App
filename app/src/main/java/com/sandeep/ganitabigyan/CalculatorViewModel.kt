// FILE: app/src/main/java/com/sandeep/ganitabigyan/CalculatorViewModel.kt
package com.sandeep.ganitabigyan

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.mXparser

// Data and Sealed classes remain the same...
data class CalculationHistory(val expression: String, val result: String)
sealed class CalculatorAction {
    data class Number(val number: String) : CalculatorAction()
    data class Operator(val symbol: String) : CalculatorAction()
    data class Scientific(val function: String) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    object Parentheses : CalculatorAction()
    object Pi : CalculatorAction()
    object E : CalculatorAction()
    object Power : CalculatorAction()
    object Factorial : CalculatorAction()
    object ToggleInv : CalculatorAction()
    object ToggleDeg : CalculatorAction()
}


class CalculatorViewModel : ViewModel() {

    var expression = mutableStateOf(TextFieldValue(""))
        private set
    var liveResult = mutableStateOf("")
        private set
    private val _history = mutableStateListOf<CalculationHistory>()
    val history: List<CalculationHistory> = _history

    var isScientificPadVisible = mutableStateOf(false)
        private set
    var isInverse = mutableStateOf(false)
        private set
    var isDegMode = mutableStateOf(true)
        private set

    // NEW: This function lets the UI manually set the cursor position.
    fun moveCursor(offset: Int) {
        expression.value = expression.value.copy(
            selection = TextRange(offset.coerceIn(0, expression.value.text.length))
        )
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> insertText(action.number)
            is CalculatorAction.Operator -> enterOperator(action.symbol)
            is CalculatorAction.Scientific -> enterScientific(action.function)
            is CalculatorAction.Decimal -> enterDecimal()
            is CalculatorAction.Clear -> {
                expression.value = TextFieldValue("")
                liveResult.value = ""
            }
            is CalculatorAction.Delete -> deleteLastCharacter()
            is CalculatorAction.Calculate -> performCalculation()
            is CalculatorAction.Parentheses -> handleParentheses()
            is CalculatorAction.Pi -> insertText("pi")
            is CalculatorAction.E -> insertText("e")
            is CalculatorAction.Power -> enterOperator("^")
            is CalculatorAction.Factorial -> enterOperator("!")
            is CalculatorAction.ToggleInv -> isInverse.value = !isInverse.value
            is CalculatorAction.ToggleDeg -> isDegMode.value = !isDegMode.value
        }
    }

    fun toggleScientificPad() { isScientificPadVisible.value = !isScientificPadVisible.value }

    fun loadFromHistory(calc: CalculationHistory) {
        expression.value = TextFieldValue(
            text = calc.expression,
            selection = TextRange(calc.expression.length)
        )
        liveResult.value = calc.result
    }

    private fun insertText(textToInsert: String) {
        val currentText = expression.value.text
        val selection = expression.value.selection
        val newText = currentText.replaceRange(selection.start, selection.end, textToInsert)
        val newCursorPos = selection.start + textToInsert.length
        expression.value = TextFieldValue(
            text = newText,
            selection = TextRange(newCursorPos)
        )
        updateLiveResult()
    }

    // ... (All other private helper functions remain the same)
    private fun enterScientific(function: String) {
        val funcName = if (isInverse.value) {
            when (function) {
                "sin" -> "asin"
                "cos" -> "acos"
                "tan" -> "atan"
                "ln" -> "log2"
                "log" -> "log10"
                else -> function
            }
        } else { function }
        insertText("$funcName(")
    }
    private fun enterOperator(symbol: String) {
        val selectionStart = expression.value.selection.start
        if (selectionStart > 0) {
            val charBefore = expression.value.text[selectionStart - 1]
            if (charBefore in listOf('+', '-', '×', '÷', '^')) {
                val newText = expression.value.text.substring(0, selectionStart - 1) + symbol + expression.value.text.substring(selectionStart)
                expression.value = TextFieldValue(newText, TextRange(selectionStart))
                updateLiveResult()
                return
            }
        }
        insertText(symbol)
    }
    private fun handleParentheses() {
        val currentText = expression.value.text
        val selection = expression.value.selection
        val openParenCount = currentText.count { it == '(' }
        val closeParenCount = currentText.count { it == ')' }
        val charBeforeCursor = currentText.getOrNull(selection.start - 1)
        if (openParenCount > closeParenCount && (charBeforeCursor?.isDigit() == true || charBeforeCursor == ')')) {
            insertText(")")
        } else { insertText("(") }
    }
    private fun enterDecimal() {
        val currentTextBeforeCursor = expression.value.text.substring(0, expression.value.selection.start)
        val lastNumberSegment = currentTextBeforeCursor.split('+', '-', '×', '÷', '(', ')', '^', '!').last()
        if (!lastNumberSegment.contains('.')) {
            insertText(".")
        }
    }
    private fun deleteLastCharacter() {
        val selection = expression.value.selection
        val currentText = expression.value.text
        if (selection.collapsed && selection.start > 0) {
            val newText = currentText.removeRange(selection.start - 1, selection.start)
            val newCursorPos = selection.start - 1
            expression.value = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
        } else if (!selection.collapsed) {
            val newText = currentText.removeRange(selection.start, selection.end)
            val newCursorPos = selection.start
            expression.value = TextFieldValue(text = newText, selection = TextRange(newCursorPos))
        }
        updateLiveResult()
    }
    private fun formatExpressionForMxParser(expr: String): String { return expr.replace("×", "*").replace("÷", "/") }
    private fun updateLiveResult() {
        var exprToCalculate = expression.value.text
        if (exprToCalculate.isNotEmpty() && exprToCalculate.last() in listOf('+', '-', '×', '÷', '^')) {
            exprToCalculate = exprToCalculate.dropLast(1)
        }
        val exprString = formatExpressionForMxParser(exprToCalculate)
        if (exprString.isBlank()) { liveResult.value = ""; return }
        if (isDegMode.value) mXparser.setDegreesMode() else mXparser.setRadiansMode()
        val expr = Expression(exprString)
        if (expr.checkSyntax()) {
            val result = expr.calculate()
            if (!result.isNaN()) { liveResult.value = formatResult(result) }
        } else {
            liveResult.value = ""
        }
    }
    private fun performCalculation() {
        if (liveResult.value.isEmpty() || liveResult.value == expression.value.text) return
        _history.add(0, CalculationHistory(expression.value.text, liveResult.value))
        val newText = liveResult.value
        expression.value = TextFieldValue(newText, TextRange(newText.length))
        liveResult.value = ""
    }
    private fun formatResult(result: Double): String {
        return if (result % 1.0 == 0.0) { result.toLong().toString() } else { String.format("%.7f", result).trimEnd('0').trimEnd('.') }
    }
}