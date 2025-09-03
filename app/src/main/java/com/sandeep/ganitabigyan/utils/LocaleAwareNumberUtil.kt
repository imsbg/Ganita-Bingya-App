// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/LocaleAwareNumberUtil.kt
// VERSION: FINAL - Includes special, correct logic for Tamil numerals.

package com.sandeep.ganitabigyan.utils

import android.content.Context
import com.sandeep.ganitabigyan.R
import java.text.SimpleDateFormat
import java.util.Locale

// --- NUMERAL CONVERTERS ---

/**
 * Converts any string containing digits to the correct numeral string based on the app's current language.
 * It now uses a more powerful method for Tamil to handle its unique numeral system.
 */
fun String.toLocaleNumerals(context: Context): String {
    val currentLang = context.resources.configuration.locales[0].language
    return when (currentLang) {
        "or" -> this.map { it.toOdiaDigit() }.joinToString("")
        "hi" -> this.map { it.toHindiDigit() }.joinToString("")
        // <<< FIX: Use a more advanced regex replacement for Tamil >>>
        // This finds each number in a string (e.g., "123" in "123 + 45") and converts it.
        "ta" -> Regex("\\d+").replace(this) { it.value.toInt().toTamilNumerals() }
        else -> this
    }
}

/**
 * Converts an integer to the correct numeral string based on the app's current language.
 * This is now the main driver for the conversion logic.
 */
fun Int.toLocaleNumerals(context: Context): String {
    val currentLang = context.resources.configuration.locales[0].language
    return when (currentLang) {
        // <<< FIX: For Tamil, call our new, dedicated function >>>
        "ta" -> this.toTamilNumerals()
        // Other languages can use the simpler string-based conversion.
        else -> this.toString().toLocaleNumerals(context)
    }
}

// --- WORD CONVERTER ---
fun Int.toWords(context: Context): String {
    val num = this
    val numberWords = context.resources.getStringArray(R.array.odia_number_words)

    if (num >= 0 && num < numberWords.size) {
        return numberWords[num]
    }
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

// --- DATE CONVERTER ---
fun String.toLocaleDate(context: Context): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val currentLocale = context.resources.configuration.locales[0]
    val outputFormat = SimpleDateFormat("d MMMM yyyy", currentLocale)
    return try {
        val date = inputFormat.parse(this)
        val formattedDate = outputFormat.format(date!!)
        formattedDate.toLocaleNumerals(context)
    } catch (e: Exception) {
        this
    }
}


// --- Private Helper Functions ---

// vvv NEW DEDICATED TAMIL CONVERTER vvv
/**
 * A special converter for integers to the traditional Tamil numeral system,
 * which uses unique characters for 10, 100, and 1000.
 */
private fun Int.toTamilNumerals(): String {
    if (this == 0) return "௦" // Zero
    if (this < 0) return "-${(-this).toTamilNumerals()}" // Handle negative numbers

    val num = this
    val tamilDigits = listOf('௦', '௧', '௨', '௩', '௪', '௫', '௬', '௭', '௮', '௯')
    val ten = '௰'
    val hundred = '௱'
    val thousand = '௲'

    val builder = StringBuilder()
    var remaining = num

    // Thousands
    if (remaining >= 1000) {
        val count = remaining / 1000
        if (count > 1) builder.append(tamilDigits[count])
        builder.append(thousand)
        remaining %= 1000
    }
    // Hundreds
    if (remaining >= 100) {
        val count = remaining / 100
        if (count > 1) builder.append(tamilDigits[count])
        builder.append(hundred)
        remaining %= 100
    }
    // Tens
    if (remaining >= 10) {
        val count = remaining / 10
        if (count > 1) builder.append(tamilDigits[count])
        builder.append(ten)
        remaining %= 10
    }
    // Units
    if (remaining > 0) {
        builder.append(tamilDigits[remaining])
    }

    return builder.toString()
}

private fun Char.toOdiaDigit(): Char {
    return when (this) { '0' -> '୦'; '1' -> '୧'; '2' -> '୨'; '3' -> '୩'; '4' -> '୪'; '5' -> '୫'; '6' -> '୬'; '7' -> '୭'; '8' -> '୮'; '9' -> '୯'; else -> this }
}

private fun Char.toHindiDigit(): Char {
    return when (this) { '0' -> '०'; '1' -> '१'; '2' -> '२'; '3' -> '३'; '4' -> '४'; '5' -> '५'; '6' -> '६'; '7' -> '७'; '8' -> '८'; '9' -> '९'; else -> this }
}

// This simple mapping is no longer used for the main conversion but can be kept for other potential uses.
private fun Char.toTamilDigit(): Char {
    return when (this) { '0' -> '௦'; '1' -> '௧'; '2' -> '௨'; '3' -> '௩'; '4' -> '௪'; '5' -> '௫'; '6' -> '௬'; '7' -> '௭'; '8' -> '௮'; '9' -> '௯'; else -> this }
}