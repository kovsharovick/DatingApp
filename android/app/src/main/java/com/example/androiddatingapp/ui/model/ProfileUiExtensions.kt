package com.example.androiddatingapp.ui.model

fun ProfileUi.hasVisibleMedia(): Boolean =
    videoUrl.isNotBlank() || thumbnailUrl.isNotBlank() || avatarUrl.isNotBlank()

fun ProfileUi.preferredVideoUrl(): String = videoUrl.ifBlank { thumbnailUrl }
