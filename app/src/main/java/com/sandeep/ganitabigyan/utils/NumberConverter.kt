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

// Your existing function to convert a date string to Odia
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

// v-- THIS IS THE NEW FUNCTION ADDED TO YOUR FILE --v
fun Int.toOdiaWord(): String {
    val num = this
    if (num == 0) return "ଶୂନ"

    val specialCases = mapOf(
        1 to "ଏକ", 2 to "ଦୁଇ", 3 to "ତିନି", 4 to "ଚାରି", 5 to "ପାଞ୍ଚ", 6 to "ଛଅ", 7 to "ସାତ", 8 to "ଆଠ", 9 to "ନଅ",
        10 to "ଦଶ", 11 to "ଏଗାର", 12 to "ବାର", 13 to "ତେର", 14 to "ଚଉଦ", 15 to "ପନ୍ଦର", 16 to "ଷୋହଳ", 17 to "ସତର", 18 to "ଅଠର", 19 to "ଉଣେଇଶି",
        20 to "କୋଡ଼ିଏ", 21 to "ଏକୋଇଶି", 22 to "ବାଇଶି", 23 to "ତେଇଶି", 24 to "ଚବିଶି", 25 to "ପଚିଶି", 26 to "ଛବିଶି", 27 to "ସତେଇଶି", 28 to "ଅଠେଇଶି", 29 to "ଅଣତିରିଶି",
        30 to "ତିରିଶି", 31 to "ଏକତିରିଶି", 32 to "ବତିଶି", 33 to "ତେତିଶି", 34 to "ଚଉତିରିଶି", 35 to "ପଞ୍ଚତିରିଶି", 36 to "ଛତିଶି", 37 to "ସଇଁତିରିଶି", 38 to "ଅଠତିରିଶି", 39 to "ଅଣଚାଳିଶି",
        40 to "ଚାଳିଶି", 41 to "ଏକଚାଳିଶି", 42 to "ବୟାଳିଶି", 43 to "ତେୟାଳିଶି", 44 to "ଚଉରାଳିଶି", 45 to "ପଞ୍ଚଚାଳିଶି", 46 to "ଛୟାଳିଶି", 47 to "ସତଚାଳିଶି", 48 to "ଅଠଚାଳିଶି", 49 to "ଅଣଚାଶ",
        50 to "ପଚାଶ", 51 to "ଏକାବନ", 52 to "ବାଉନ", 53 to "ତେପନ", 54 to "ଚଉବନ", 55 to "ପଞ୍ଚାବନ", 56 to "ଛପନ", 57 to "ସତାବନ", 58 to "ଅଠାବନ", 59 to "ଅଣଷଠି",
        60 to "ଷାଠିଏ", 61 to "ଏକଷଠି", 62 to "ବାଷଠି", 63 to "ତେଷଠି", 64 to "ଚଉଷଠି", 65 to "ପଞ୍ଚଷଠି", 66 to "ଛଅଷଠି", 67 to "ସତଷଠି", 68 to "ଅଠଷଠି", 69 to "ଅଣସ୍ତରି",
        70 to "ସତୁରି", 71 to "ଏକସ୍ତରି", 72 to "ବାସ୍ତରି", 73 to "ତେସ୍ତରି", 74 to "ଚଉସ୍ତରି", 75 to "ପଞ୍ଚସ୍ତରି", 76 to "ଛଅସ୍ତରି", 77 to "ସତସ୍ତରି", 78 to "ଅଠସ୍ତରି", 79 to "ଅଣାଅଶୀ",
        80 to "ଅଶୀ", 81 to "ଏକାଅଶୀ", 82 to "ବୟାଅଶୀ", 83 to "ତେୟାଅଶୀ", 84 to "ଚଉରାଅଶୀ", 85 to "ପଞ୍ଚାଅଶୀ", 86 to "ଛୟାଅଶୀ", 87 to "ସତାଅଶୀ", 88 to "ଅଠାଅଶୀ", 89 to "ଅଣାନବେ",
        90 to "ନବେ", 91 to "ଏକାନବେ", 92 to "ବୟାନବେ", 93 to "ତେୟାନବେ", 94 to "ଚଉରାନବେ", 95 to "ପଞ୍ଚାନବେ", 96 to "ଛୟାନବେ", 97 to "ସତାନବେ", 98 to "ଅଠାନବେ", 99 to "ଅନେଶୋତ",
        100 to "ଶହେ"
    )
    if (specialCases.containsKey(num)) return specialCases[num]!!

    if (num > 100) {
        val hundredPart = num / 100
        val remainder = num % 100

        val hundredWordPart = if (hundredPart == 1) {
            "ଶହେ"
        } else {
            "${hundredPart.toOdiaWord()} ଶହ"
        }

        return if (remainder == 0) {
            hundredWordPart
        } else {
            "$hundredWordPart ${remainder.toOdiaWord()}"
        }
    }
    return num.toOdia()
}
// ^-- THIS IS THE NEW FUNCTION ADDED TO YOUR FILE --^