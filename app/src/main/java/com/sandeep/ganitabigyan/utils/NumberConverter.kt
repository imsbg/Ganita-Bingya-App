package com.sandeep.ganitabigyan.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Converts an integer to a string with Odia numerals.
fun Int.toOdia(): String {
    return this.toString().toOdiaNumerals()
}

// Converts any string containing English digits to a string with Odia numerals.
fun String.toOdiaNumerals(): String {
    val odiaDigits = listOf('୦', '୧', '୨', '୩', '୪', '୫', '୬', '୭', '୮', '୯')
    return this.map { char ->
        if (char.isDigit()) {
            odiaDigits[Character.getNumericValue(char)]
        } else {
            char
        }
    }.joinToString("")
}

// NEW: Function to convert a date string like "August 06, 2025" to Odia
fun String.toOdiaDate(): String {
    val monthMap = mapOf(
        "January" to "ଜାନୁଆରୀ", "February" to "ଫେବୃଆରୀ", "March" to "ମାର୍ଚ୍ଚ",
        "April" to "ଏପ୍ରିଲ", "May" to "ମେ", "June" to "ଜୁନ୍",
        "July" to "ଜୁଲାଇ", "August" to "ଅଗଷ୍ଟ", "September" to "ସେପ୍ଟେମ୍ବର",
        "October" to "ଅକ୍ଟୋବର", "November" to "ନଭେମ୍ବର", "December" to "ଡିସେମ୍ବର"
    )

    // Split "August 06, 2025" into parts
    val parts = this.replace(",", "").split(" ")
    if (parts.size != 3) return this // Return original if format is unexpected

    val month = monthMap[parts[0]] ?: parts[0]
    val day = parts[1].toOdiaNumerals()
    val year = parts[2].toOdiaNumerals()

    return "$month $day, $year"
}