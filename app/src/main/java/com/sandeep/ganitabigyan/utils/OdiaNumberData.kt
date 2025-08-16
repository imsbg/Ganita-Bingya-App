package com.sandeep.ganitabigyan.utils

data class OdiaNumberWord(
    val numeral: String,
    val word: String
)

fun getOdiaNumberList(): List<OdiaNumberWord> {
    val list = mutableListOf<OdiaNumberWord>()
    for (i in 1..100) {
        list.add(
            OdiaNumberWord(
                numeral = i.toOdia(),
                word = i.toOdiaWord()
            )
        )
    }
    return list
}