package com.example.androiddatingapp.ui.home

import com.example.androiddatingapp.ui.model.ProfileUi

/** Лента: без уже свайпнутых; сначала те, кто уже свайпнул вас. */
fun List<ProfileUi>.forFeedDisplay(swipedUserIds: Set<Long>): List<ProfileUi> =
    asSequence()
        .filter { it.userId !in swipedUserIds }
        .sortedByDescending { it.likedYou }
        .toList()

fun Throwable.isAlreadySwipedError(): Boolean =
    message?.contains("already swiped", ignoreCase = true) == true
