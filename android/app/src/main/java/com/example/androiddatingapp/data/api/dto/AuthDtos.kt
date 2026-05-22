package com.example.androiddatingapp.data.api.dto

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val dateOfBirth: String,
    val gender: String,
    val city: String,
    val description: String? = null,
)

data class AuthResponse(
    val userId: Long,
    val token: String,
    val tokenType: String = "Bearer",
)

data class UserProfileResponse(
    val id: Long,
    val name: String,
    val age: Int,
    val city: String,
    val region: String? = null,
    val description: String? = null,
    val videoUrl: String? = null,
    val hidden: Boolean = false,
)

data class ApiErrorBody(
    val error: String? = null,
    val errors: Map<String, Any>? = null,
)
