package com.example.androiddatingapp.ui.model

import com.example.androiddatingapp.ui.util.DateOfBirthInput
import java.time.LocalDate

data class UserAccount(
    val userId: Long? = null,
    val authToken: String? = null,
    val email: String,
    val password: String = "",
    val name: String,
    val dateOfBirth: String,
    val gender: Gender,
    val city: String,
    val description: String = "",
    val hasVideo: Boolean = false,
    val videoTitle: String = "",
    val isProfileActive: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val likesUsedToday: Int = 0,
    val bonusLikes: Int = 0,
    val likeQuotaDay: String = "",
) {
    fun ageYears(): Int? = DateOfBirthInput.parseAgeYears(dateOfBirth)

    fun canUseFeed(): Boolean = hasVideo && isProfileActive

    fun canUseMessages(): Boolean = true

    fun remainingLikes(): Int {
        val today = todayKey()
        val usedToday = if (likeQuotaDay == today) likesUsedToday else 0
        val freeLeft = (DAILY_FREE_LIKES - usedToday).coerceAtLeast(0)
        return freeLeft + bonusLikes
    }

    fun canLike(): Boolean = remainingLikes() > 0

    fun withLikeConsumed(): UserAccount {
        if (!canLike()) return this
        val today = todayKey()
        val normalized = normalizeLikeDay(today)
        val usedToday = normalized.likesUsedToday
        if (usedToday < DAILY_FREE_LIKES) {
            return normalized.copy(likesUsedToday = usedToday + 1)
        }
        return normalized.copy(bonusLikes = normalized.bonusLikes - 1)
    }

    fun withBonusLikesPurchased(amount: Int): UserAccount {
        require(amount > 0)
        val normalized = normalizeLikeDay(todayKey())
        return normalized.copy(bonusLikes = normalized.bonusLikes + amount)
    }

    private fun normalizeLikeDay(today: String): UserAccount =
        if (likeQuotaDay == today) this else copy(likesUsedToday = 0, likeQuotaDay = today)

    companion object {
        const val DAILY_FREE_LIKES = 20
        val LIKE_PACK_OPTIONS = listOf(10, 20, 50)

        private fun todayKey(): String = LocalDate.now().toString()
    }
}
