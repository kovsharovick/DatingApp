package com.example.androiddatingapp.ui.model

data class ProfileUi(
    val userId: Long,
    val name: String,
    val age: Int,
    val city: String,
    val description: String = "",
)
