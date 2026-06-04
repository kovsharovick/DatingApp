package com.example.androiddatingapp.ui.profile

import com.example.androiddatingapp.ui.model.Gender

data class UserProfileUi(
    val name: String,
    val dateOfBirth: String,
    val city: String,
    val description: String,
    val gender: Gender,
    val videoTitle: String,
    val videoUrl: String = "",
    val avatarUrl: String = "",
)
