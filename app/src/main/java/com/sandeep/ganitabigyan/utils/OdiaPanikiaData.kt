// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/OdiaPanikiaData.kt

package com.sandeep.ganitabigyan.utils

import android.content.Context
import com.sandeep.ganitabigyan.R

// A data class to hold both numerical and script versions
data class PanikiaFullRow(
    val numericalExpression: String,
    val numericalResult: String,
    val scriptLine: String
)

// The function now requires a 'Context' to access resources
fun getOdiaPanikiaTable(tableNumber: Int, context: Context): List<PanikiaFullRow> {
    val table = mutableListOf<PanikiaFullRow>()
    val odiaTableNumber = tableNumber.toOdia()
    val tableNumberWord = tableNumber.toOdiaWord()

    // Load the translatable panikia terms from strings.xml
    val panikiaTerms = context.resources.getStringArray(R.array.panikia_terms)

    for (i in 1..10) {
        val resultInt = tableNumber * i

        // 1. Generate the Odia Numerical Version
        val numericalExpression = "$odiaTableNumber × ${i.toOdia()}"
        val numericalResult = resultInt.toOdia()

        // 2. Generate the full Odia Script Line
        val resultWord = resultInt.toOdiaWord()

        // Get the correct term from the array we loaded
        // We use (i-1) because arrays are 0-indexed (item 1 is at position 0)
        val scriptExpressionPart = if (i > 0 && i <= panikiaTerms.size) {
            panikiaTerms[i - 1]
        } else {
            "" // Fallback for safety
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