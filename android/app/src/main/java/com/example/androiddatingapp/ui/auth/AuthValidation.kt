package com.example.androiddatingapp.ui.auth

import com.example.androiddatingapp.ui.model.Gender

object AuthValidation {
    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun validateLogin(email: String, password: String): String? {
        if (email.isBlank()) return "Введите email"
        if (!EMAIL_REGEX.matches(email.trim())) return "Некорректный email"
        if (password.isBlank()) return "Введите пароль"
        return null
    }

    fun validateRegisterCredentials(email: String, password: String): String? {
        if (email.isBlank()) return "Введите email"
        if (!EMAIL_REGEX.matches(email.trim())) return "Некорректный email"
        if (password.length < 6) return "Пароль — минимум 6 символов"
        return null
    }

    fun validateRegisterProfile(
        name: String,
        dateOfBirth: String,
        gender: Gender?,
        city: String,
    ): String? {
        if (name.isBlank()) return "Введите имя"
        if (dateOfBirth.isBlank()) return "Введите дату рождения"
        if (gender == null) return "Выберите пол"
        if (city.isBlank()) return "Введите город"
        return null
    }
}
