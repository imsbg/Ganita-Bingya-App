// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/NumberDataGenerator.kt
// VERSION: FINAL - This is the correct file.

package com.sandeep.ganitabigyan.utils

import android.content.Context

// <<< THIS IS THE NEW DATA CLASS NAME >>>
data class NumberWordPair(
    val numeral: String,
    val word: String
)

/**
 * Generates a list of numbers from 1 to 100 with their corresponding
 * numerals and words for the app's current language.
 */
fun getNumberList(context: Context): List<NumberWordPair> {
    val list = mutableListOf<NumberWordPair>()
    for (i in 0..100) {
        list.add(
            NumberWordPair(
                numeral = i.toLocaleNumerals(context),
                word = i.toWords(context)
            )
        )
    }
    return list
}