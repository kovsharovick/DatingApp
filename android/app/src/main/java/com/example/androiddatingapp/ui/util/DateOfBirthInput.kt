package com.example.androiddatingapp.ui.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateOfBirthInput {
    const val PATTERN = "dd-MM-yyyy"
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern(PATTERN)

    /** ДД-ММ-ГГГГ из строки только из цифр (до 8). */
    fun formatFromDigits(digits: String): String {
        val d = digits.filter { it.isDigit() }.take(8)
        if (d.isEmpty()) return ""

        val day = d.take(2)
        val month = d.drop(2).take(2)
        val year = d.drop(4)

        return buildString {
            append(day)
            if (d.length > 2) {
                append('-')
                append(month)
            }
            if (d.length > 4) {
                append('-')
                append(year)
            }
        }
    }

    /** Обновляет буфер цифр с учётом вставки, удаления и вставки из буфера. */
    fun updateDigits(currentDigits: String, fieldText: String): String {
        val extracted = fieldText.filter { it.isDigit() }.take(8)
        val current = currentDigits.filter { it.isDigit() }.take(8)

        return when {
            extracted.isEmpty() -> ""
            extracted.length <= current.length -> extracted
            extracted.length == current.length + 1 -> current + extracted.last()
            else -> extracted
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseAgeYears(formatted: String): Int? {
        if (formatted.length != PATTERN.length) return null
        return try {
            val dob = LocalDate.parse(formatted, formatter)
            Period.between(dob, LocalDate.now()).years
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
