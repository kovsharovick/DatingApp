package com.example.androiddatingapp.ui.util

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateOfBirthInput {
    /** Формат ввода в приложении. */
    const val UI_PATTERN = "dd-MM-yyyy"
    const val PATTERN = UI_PATTERN

    /** Формат для API и БД (ISO). */
    const val API_PATTERN = "yyyy-MM-dd"

    private val uiFormatter = DateTimeFormatter.ofPattern(UI_PATTERN)
    private val apiFormatter = DateTimeFormatter.ISO_LOCAL_DATE

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

    fun parseAgeYears(formatted: String): Int? {
        if (formatted.length != UI_PATTERN.length) return null
        return try {
            val dob = LocalDate.parse(formatted, uiFormatter)
            Period.between(dob, LocalDate.now()).years
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /** Дата рождения для заданного возраста (день/месяц — «сегодня» минус N лет). */
    fun formatForAgeYears(ageYears: Int): String? {
        if (ageYears !in 18..99) return null
        return uiFormatter.format(LocalDate.now().minusYears(ageYears.toLong()))
    }

    fun validateDateOfBirth(formatted: String): String? {
        if (formatted.length != UI_PATTERN.length) return "Введите дату в формате ДД-ММ-ГГГГ"
        val age = parseAgeYears(formatted) ?: return "Некорректная дата"
        if (age !in 18..99) return "Возраст от 18 до 99 лет"
        return null
    }

    /**
     * Конвертация для отправки на бэкенд: [UI_PATTERN] → [API_PATTERN].
     * Проверяет, что дата существует (например, 31-02-2005 будет отклонена).
     */
    fun toApiIsoDate(uiFormatted: String): String? {
        if (uiFormatted.length != UI_PATTERN.length) return null
        return try {
            val date = LocalDate.parse(uiFormatted, uiFormatter)
            apiFormatter.format(date)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /** Обратная конвертация, когда API вернёт ISO-дату (для отображения в поле). */
    fun fromApiIsoDate(apiFormatted: String): String? {
        if (apiFormatted.isBlank()) return null
        return try {
            val date = LocalDate.parse(apiFormatted, apiFormatter)
            uiFormatter.format(date)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
