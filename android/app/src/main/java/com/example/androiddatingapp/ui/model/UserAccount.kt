package com.example.androiddatingapp.ui.model

import com.example.androiddatingapp.ui.util.DateOfBirthInput
import java.time.LocalDate

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
    val onboardingCompleted: Boolean = false,
    val swipesUsedToday: Int = 0,
    val bonusSwipes: Int = 0,
    val swipeQuotaDay: String = "",
) {
    fun ageYears(): Int? = DateOfBirthInput.parseAgeYears(dateOfBirth)

    fun canUseFeed(): Boolean = hasVideo && isProfileActive

    fun canUseMessages(): Boolean = true

    fun remainingSwipes(): Int {
        val today = todayKey()
        val usedToday = if (swipeQuotaDay == today) swipesUsedToday else 0
        val freeLeft = (DAILY_FREE_SWIPES - usedToday).coerceAtLeast(0)
        return freeLeft + bonusSwipes
    }

    fun canSwipe(): Boolean = remainingSwipes() > 0

    fun withSwipeConsumed(): UserAccount {
        if (!canSwipe()) return this
        val today = todayKey()
        val normalized = normalizeSwipeDay(today)
        val usedToday = normalized.swipesUsedToday
        if (usedToday < DAILY_FREE_SWIPES) {
            return normalized.copy(swipesUsedToday = usedToday + 1)
        }
        return normalized.copy(bonusSwipes = normalized.bonusSwipes - 1)
    }

    fun withBonusSwipesPurchased(amount: Int): UserAccount {
        require(amount > 0)
        val normalized = normalizeSwipeDay(todayKey())
        return normalized.copy(bonusSwipes = normalized.bonusSwipes + amount)
    }

    private fun normalizeSwipeDay(today: String): UserAccount =
        if (swipeQuotaDay == today) this else copy(swipesUsedToday = 0, swipeQuotaDay = today)

    companion object {
        const val DAILY_FREE_SWIPES = 20
        val SWIPE_PACK_OPTIONS = listOf(10, 20, 50)

        private fun todayKey(): String = LocalDate.now().toString()
    }
}
