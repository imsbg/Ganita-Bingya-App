// FILE: app/src/main/java/com/sandeep/ganitabigyan/utils/LocaleAwareNumberUtil.kt

package com.sandeep.ganitabigyan.utils

import android.content.Context
import com.sandeep.ganitabigyan.R
import java.text.SimpleDateFormat
import java.util.Locale

// Numeral and Date converters are unchanged and correct
fun String.toLocaleNumerals(context: Context): String {
    val currentLang = context.resources.configuration.locales[0].language
    return when (currentLang) {
        "or", "sat" -> this.map { it.toOdiaDigit() }.joinToString("")
        "hi", "sa" -> this.map { it.toHindiDigit() }.joinToString("")
        "bn", "as" -> this.map { it.toBengaliDigit() }.joinToString("") // Assamese ('as') added here
        "gu" -> this.map { it.toGujaratiDigit() }.joinToString("")
        "te" -> this.map { it.toTeluguDigit() }.joinToString("")
        else -> this
    }
}

fun Int.toLocaleNumerals(context: Context): String {
    return this.toString().toLocaleNumerals(context)
}

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


// <<< THIS IS THE FINAL, CORRECTED toWords FUNCTION >>>
fun Int.toWords(context: Context): String {
    val num = this
    val numberWords = context.resources.getStringArray(R.array.odia_number_words)

    if (num in 0..99) {
        return numberWords[num]
    }

    val currentLang = context.resources.configuration.locales[0].language
    val isOdiaBased = currentLang == "or" || currentLang == "sat"

    if (num == 100) {
        return if (isOdiaBased) {
            context.getString(R.string.word_hundred_odia_standalone) // "ଶହେ"
        } else {
            // "One" + " " + "Hundred"
            "${numberWords[1]} ${context.getString(R.string.word_hundreds_suffix)}"
        }
    }

    if (num in 101..999) {
        val hundredPart = num / 100
        val remainder = num % 100

        val hundredWordPart = if (isOdiaBased && hundredPart == 1) {
            // For Odia numbers like 106, use "ଶହେ" (sahe)
            context.getString(R.string.word_hundred_odia_standalone)
        } else {
            // For all other languages, AND for Odia numbers like 206, 306, etc.
            // Build it like "Two" + "Hundred" or "ଦୁଇ" + "ଶହ"
            "${hundredPart.toWords(context)} ${context.getString(R.string.word_hundreds_suffix)}"
        }

        return if (remainder == 0) {
            hundredWordPart
        } else {
            "$hundredWordPart ${remainder.toWords(context)}"
        }
    }

    // Fallback for numbers >= 1000 or negative numbers
    return this.toLocaleNumerals(context)
}


// --- Private Helper Functions (unchanged) ---
private fun Char.toOdiaDigit(): Char {
    return when (this) {
        '0' -> '୦'; '1' -> '୧'; '2' -> '୨'; '3' -> '୩'; '4' -> '୪'; '5' -> '୫'; '6' -> '୬'; '7' -> '୭'; '8' -> '୮'; '9' -> '୯'; else -> this
    }
}

private fun Char.toHindiDigit(): Char {
    return when (this) {
        '0' -> '०'; '1' -> '१'; '2' -> '२'; '3' -> '३'; '4' -> '४'; '5' -> '५'; '6' -> '६'; '7' -> '७'; '8' -> '८'; '9' -> '९'; else -> this
    }
}

private fun Char.toBengaliDigit(): Char {
    return when (this) {
        '0' -> '০'; '1' -> '১'; '2' -> '২'; '3' -> '৩'; '4' -> '৪'; '5' -> '৫'; '6' -> '৬'; '7' -> '৭'; '8' -> '৮'; '9' -> '৯'; else -> this
    }
}

private fun Char.toGujaratiDigit(): Char {
    return when (this) {
        '0' -> '૦'; '1' -> '૧'; '2' -> '૨'; '3' -> '૩'; '4' -> '૪'; '5' -> '૫'; '6' -> '૬'; '7' -> '૭'; '8' -> '૮'; '9' -> '૯'; else -> this
    }
}

private fun Char.toTeluguDigit(): Char {
    return when (this) {
        '0' -> '౦'; '1' -> '౧'; '2' -> '౨'; '3' -> '౩'; '4' -> '౪'; '5' -> '౫'; '6' -> '౬'; '7' -> '౭'; '8' -> '౮'; '9' -> '౯'; else -> this
    }
}