// FILE: app/src/main/java/com/sandeep/ganitabigyan/OdiaNumberUtil.kt
package com.sandeep.ganitabigyan

// A simple utility to convert English digits to Odia digits in a string.
object OdiaNumberUtil {
    fun toOdia(input: String): String {
        return input
            .replace('0', '୦')
            .replace('1', '୧')
            .replace('2', '୨')
            .replace('3', '୩')
            .replace('4', '୪')
            .replace('5', '୫')
            .replace('6', '୬')
            .replace('7', '୭')
            .replace('8', '୮')
            .replace('9', '୯')
    }
}