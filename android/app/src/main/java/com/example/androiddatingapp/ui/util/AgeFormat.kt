package com.example.androiddatingapp.ui.util

fun formatAgeLabel(ageYears: Int): String {
    val mod100 = ageYears % 100
    val mod10 = ageYears % 10
    val word = when {
        mod100 in 11..14 -> "лет"
        mod10 == 1 -> "год"
        mod10 in 2..4 -> "года"
        else -> "лет"
    }
    return "$ageYears $word"
}
