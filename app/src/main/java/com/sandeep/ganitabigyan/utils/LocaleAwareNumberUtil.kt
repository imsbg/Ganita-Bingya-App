// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/LocaleAwareNumberUtil.kt
// VERSION: FINAL - Tamil and Punjabi have been removed.

package com.sandeep.ganitabigyan.utils

import android.content.Context
import com.sandeep.ganitabigyan.R
import java.text.SimpleDateFormat
import java.util.Locale

// --- NUMERAL CONVERTERS ---

/**
 * Converts any string containing digits to the correct numeral string based on the app's current language.
 */
fun String.toLocaleNumerals(context: Context): String {
    val currentLang = context.resources.configuration.locales[0].language
    return when (currentLang) {
        "or", "sat" -> this.map { it.toOdiaDigit() }.joinToString("") // Odia & Santali
        "hi", "sa" -> this.map { it.toHindiDigit() }.joinToString("")  // Hindi & Sanskrit
        "bn" -> this.map { it.toBengaliDigit() }.joinToString("") // Bengali
        "gu" -> this.map { it.toGujaratiDigit() }.joinToString("")// Gujarati
        "te" -> this.map { it.toTeluguDigit() }.joinToString("")  // Telugu
        else -> this // Default for English and others
    }
}

/**
 * Converts an integer to the correct numeral string based on the app's current language.
 */
fun Int.toLocaleNumerals(context: Context): String {
    // All remaining languages use the simple String converter
    return this.toString().toLocaleNumerals(context)
}

// --- WORD CONVERTER (Unchanged) ---
fun Int.toWords(context: Context): String {
    val num = this
    val numberWords = context.resources.getStringArray(R.array.odia_number_words)
    if (num >= 0 && num < numberWords.size) { return numberWords[num] }
    if (num >= 100 && num < 1000) {
        val hundredPart = num / 100
        val remainder = num % 100
        val oneHundred = context.getString(R.string.word_one_hundred)
        val hundredsSuffix = context.getString(R.string.word_hundreds_suffix)
        val hundredWordPart = if (hundredPart == 1) oneHundred else "${hundredPart.toWords(context)} $hundredsSuffix"
        return if (remainder == 0) hundredWordPart else "$hundredWordPart ${remainder.toWords(context)}"
    }
    return this.toLocaleNumerals(context)
}

// --- DATE CONVERTER (Unchanged) ---
fun String.toLocaleDate(context: Context): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val currentLocale = context.resources.configuration.locales[0]
    val outputFormat = SimpleDateFormat("d MMMM yyyy", currentLocale)
    return try {
        val date = inputFormat.parse(this)
        val formattedDate = outputFormat.format(date!!)
        formattedDate.toLocaleNumerals(context)
    } catch (e: Exception) { this }
}


// --- Private Helper Functions ---

private fun Char.toOdiaDigit(): Char {
    return when (this) { '0' -> '୦'; '1' -> '୧'; '2' -> '୨'; '3' -> '୩'; '4' -> '୪'; '5' -> '୫'; '6' -> '୬'; '7' -> '୭'; '8' -> '୮'; '9' -> '୯'; else -> this }
}

private fun Char.toHindiDigit(): Char {
    return when (this) { '0' -> '०'; '1' -> '१'; '2' -> '२'; '3' -> '३'; '4' -> '४'; '5' -> '५'; '6' -> '६'; '7' -> '७'; '8' -> '८'; '9' -> '९'; else -> this }
}

private fun Char.toBengaliDigit(): Char {
    return when (this) { '0' -> '০'; '1' -> '১'; '2' -> '২'; '3' -> '৩'; '4' -> '৪'; '5' -> '৫'; '6' -> '৬'; '7' -> '৭'; '8' -> '৮'; '9' -> '৯'; else -> this }
}

private fun Char.toGujaratiDigit(): Char {
    return when (this) { '0' -> '૦'; '1' -> '૧'; '2' -> '૨'; '3' -> '૩'; '4' -> '૪'; '5' -> '૫'; '6' -> '૬'; '7' -> '૭'; '8' -> '૮'; '9' -> '૯'; else -> this }
}

private fun Char.toTeluguDigit(): Char {
    return when (this) { '0' -> '౦'; '1' -> '౧'; '2' -> '౨'; '3' -> '౩'; '4' -> '౪'; '5' -> '౫'; '6' -> '౬'; '7' -> '౭'; '8' -> '౮'; '9' -> '౯'; else -> this }
}