// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/PanikiaGenerator.kt
// VERSION: FINAL - Provides separate parts for the script line.

package com.sandeep.ganitabigyan.utils

import android.content.Context
import com.sandeep.ganitabigyan.R

// <<< FIX: The data class now holds the parts of the script line separately >>>
data class PanikiaRow(
    val numericalExpression: String,
    val numericalResult: String,
    val scriptPart1: String, // e.g., "Twenty-one"
    val scriptPart2: String, // e.g., "times five is"
    val scriptPart3: String  // e.g., "One hundred five"
)

fun getPanikiaTable(tableNumber: Int, context: Context): List<PanikiaRow> {
    val table = mutableListOf<PanikiaRow>()
    val panikiaTerms = context.resources.getStringArray(R.array.panikia_terms)
    val tableNumberWord = tableNumber.toWords(context)

    for (i in 1..10) {
        val resultInt = tableNumber * i
        val numericalExpression = "${tableNumber.toLocaleNumerals(context)} × ${i.toLocaleNumerals(context)}"
        val numericalResult = resultInt.toLocaleNumerals(context)
        val resultWord = resultInt.toWords(context)
        val scriptTerm = panikiaTerms.getOrNull(i - 1) ?: ""

        table.add(
            PanikiaRow(
                numericalExpression = numericalExpression,
                numericalResult = numericalResult,
                scriptPart1 = tableNumberWord,
                scriptPart2 = scriptTerm,
                scriptPart3 = resultWord
            )
        )
    }
    return table
}