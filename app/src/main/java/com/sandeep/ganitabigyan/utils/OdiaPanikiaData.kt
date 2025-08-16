package com.sandeep.ganitabigyan.utils

// A new data class to hold both numerical and script versions
data class PanikiaFullRow(
    val numericalExpression: String,
    val numericalResult: String,
    val scriptLine: String
)

fun getOdiaPanikiaTable(tableNumber: Int): List<PanikiaFullRow> {
    val table = mutableListOf<PanikiaFullRow>()
    val odiaTableNumber = tableNumber.toOdia()
    // CORRECT: Now calls the public function from NumberConverter.kt
    val tableNumberWord = tableNumber.toOdiaWord()

    for (i in 1..10) {
        val resultInt = tableNumber * i

        // 1. Generate the Odia Numerical Version
        val numericalExpression = "$odiaTableNumber × ${i.toOdia()}"
        val numericalResult = resultInt.toOdia()

        // 2. Generate the full Odia Script Line
        // CORRECT: Now calls the public function from NumberConverter.kt
        val resultWord = resultInt.toOdiaWord()
        // This 'when' block contains YOUR specific panikia terms
        val scriptExpressionPart = when (i) {
            1 -> "କେ"
            2 -> "ଦୁଗୁଣେ"
            3 -> "ତିରି"
            4 -> "ଚଉ"
            5 -> "ପଞ୍ଚା"
            6 -> "ସୋ" // Your custom term is preserved
            7 -> "ସତା"
            8 -> "ଅଷ୍ଟା"
            9 -> "ନୂଆଁ"
            10 -> "ଦଶା"
            else -> ""
        }
        val scriptLine = "$tableNumberWord $scriptExpressionPart $resultWord"

        table.add(
            PanikiaFullRow(
                numericalExpression, numericalResult, scriptLine
            )
        )
    }
    return table
}

// The private getOdiaWordForPanikia function has been removed from this file.