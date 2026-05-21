package com.example.androiddatingapp.ui.model

import com.example.androiddatingapp.ui.util.DateOfBirthInput

data class UserAccount(
    val email: String,
    val password: String,
    val name: String,
    val dateOfBirth: String,
    val gender: Gender,
    val city: String,
    val description: String = "",
    val hasVideo: Boolean = false,
    val videoTitle: String = "",
    val isProfileActive: Boolean = true,
    val avatarSeed: Int = 0,
    val onboardingCompleted: Boolean = false,
) {
    fun ageYears(): Int? = DateOfBirthInput.parseAgeYears(dateOfBirth)

    fun canUseFeed(): Boolean = hasVideo && isProfileActive

    fun canUseMessages(): Boolean = hasVideo
}
