package com.example.androiddatingapp.data

import com.example.androiddatingapp.data.api.ApiClient
import com.example.androiddatingapp.data.api.DatingApiService
import com.example.androiddatingapp.data.api.dto.LoginRequest
import com.example.androiddatingapp.data.api.dto.RegisterRequest
import com.example.androiddatingapp.data.api.dto.UserProfileResponse
import com.example.androiddatingapp.ui.model.Gender
import com.example.androiddatingapp.ui.model.UserAccount
import com.example.androiddatingapp.ui.util.DateOfBirthInput

class AuthRepository(
    private val api: DatingApiService = ApiClient.api,
    private val sessionStore: SessionStore,
) {

    init {
        ApiClient.setAuthTokenProvider { sessionStore.token }
    }

    suspend fun restoreSession(): Result<UserAccount> {
        val stored = sessionStore.load() ?: return Result.failure(IllegalStateException("Нет сохранённой сессии"))
        return runCatching { fetchProfile(stored.email) }
            .mapError()
            .also { result ->
                if (result.isFailure) sessionStore.clear()
            }
    }

    suspend fun login(email: String, password: String): Result<UserAccount> {
        return runCatching {
            val auth = api.login(LoginRequest(email = email.trim(), password = password))
            sessionStore.save(
                StoredSession(
                    userId = auth.userId,
                    token = auth.token,
                    email = email.trim().lowercase(),
                ),
            )
            fetchProfile(email.trim().lowercase())
        }.mapError()
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        dateOfBirth: String,
        gender: Gender,
        city: String,
    ): Result<UserAccount> {
        val uiDateOfBirth = dateOfBirth.trim()
        val apiDateOfBirth = DateOfBirthInput.toApiIsoDate(uiDateOfBirth)
            ?: return Result.failure(Exception("Некорректная дата рождения"))
        return runCatching {
            val auth = api.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    name = name.trim(),
                    // UI: dd-MM-yyyy → API/БД: yyyy-MM-dd
                    dateOfBirth = apiDateOfBirth,
                    gender = gender.name,
                    city = city.trim(),
                ),
            )
            sessionStore.save(
                StoredSession(
                    userId = auth.userId,
                    token = auth.token,
                    email = email.trim().lowercase(),
                ),
            )
            fetchProfile(
                email = email.trim().lowercase(),
                dateOfBirth = uiDateOfBirth,
                gender = gender,
            )
        }.mapError()
    }

    suspend fun searchCities(prefix: String): Result<List<String>> {
        return runCatching {
            api.searchCities(prefix.trim())
        }.mapError()
    }

    suspend fun logout() {
        sessionStore.clear()
    }

    private suspend fun fetchProfile(
        email: String,
        dateOfBirth: String = "",
        gender: Gender = Gender.MALE,
    ): UserAccount {
        val profile = api.getProfile()
        return profile.toUserAccount(email).copy(
            dateOfBirth = dateOfBirth,
            gender = gender,
        )
    }

    private fun UserProfileResponse.toUserAccount(email: String): UserAccount = UserAccount(
        userId = id,
        authToken = sessionStore.token,
        email = email,
        name = name,
        dateOfBirth = "", // бэкенд пока не отдаёт дату рождения в /me
        gender = Gender.MALE, // бэкенд пока не отдаёт пол в /me
        city = city,
        description = description.orEmpty(),
        hasVideo = !videoUrl.isNullOrBlank(),
        videoTitle = if (!videoUrl.isNullOrBlank()) "profile_video" else "",
        isProfileActive = !hidden,
        onboardingCompleted = true,
    )

    private fun <T> Result<T>.mapError(): Result<T> =
        exceptionOrNull()?.let { Result.failure(Exception(ApiClient.parseErrorMessage(it))) }
            ?: this
}
