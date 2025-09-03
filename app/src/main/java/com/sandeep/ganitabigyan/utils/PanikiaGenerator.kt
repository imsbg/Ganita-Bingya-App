// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/PanikiaGenerator.kt
// VERSION: FINAL - Fully multilingual panikia/table generator.

package com.sandeep.ganitabigyan.utils

import android.content.Context
import com.sandeep.ganitabigyan.R

// Generic data class for a row in the multiplication table
data class PanikiaRow(
    val numericalExpression: String,
    val numericalResult: String,
    val scriptLine: String
)

/**
 * Generates a full multiplication table (Panikia) for a given number
 * in the app's current language using string resources.
 */
fun getPanikiaTable(tableNumber: Int, context: Context): List<PanikiaRow> {
    val table = mutableListOf<PanikiaRow>()

    // Get the localized terms and formats from resources
    val panikiaTerms = context.resources.getStringArray(R.array.panikia_terms)
    val tableNumberWord = tableNumber.toWords(context)

    for (i in 1..10) {
        val resultInt = tableNumber * i

        // 1. Generate the Numerical Version for the current language
        val numericalExpression = "${tableNumber.toLocaleNumerals(context)} × ${i.toLocaleNumerals(context)}"
        val numericalResult = resultInt.toLocaleNumerals(context)

        // 2. Generate the full Script Line
        val resultWord = resultInt.toWords(context)

        // Get the correct term (e.g., "ଏକେ", "ਦੂਣੀ", "ones are")
        // Arrays are 0-indexed, so we use (i-1)
        val scriptTerm = panikiaTerms.getOrNull(i - 1) ?: ""

        val scriptLine = "$tableNumberWord $scriptTerm $resultWord"

        table.add(
            PanikiaRow(
                numericalExpression, numericalResult, scriptLine
            )
        )
    }
    return table
}