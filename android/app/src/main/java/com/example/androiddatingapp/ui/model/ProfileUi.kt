package com.example.androiddatingapp.ui.model

data class ProfileUi(
    val userId: Long,
    val name: String,
    val age: Int,
    val city: String,
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val avatarUrl: String = "",
    /** На сервере: этот пользователь уже свайпнул вас (приоритет в ленте). */
    val likedYou: Boolean = false,
)
