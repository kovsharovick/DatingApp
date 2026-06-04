package com.example.androiddatingapp.ui.model

data class UserPreferences(
    val preferredGenders: Set<Gender>,
    val minAge: Int,
    val maxAge: Int,
    val radiusKm: Int,
) {
    fun toApiGenderList(): List<String> = preferredGenders.map { it.name }.sorted()

    fun summaryLine(city: String): String {
        val genders = preferredGenders.joinToString(", ") { it.labelRu }
        return "$genders · $minAge–$maxAge лет · $radiusKm км от $city"
    }

    fun clampForApi(): UserPreferences = copy(
        radiusKm = radiusKm.coerceIn(MIN_RADIUS_KM, MAX_RADIUS_KM),
        minAge = minAge.coerceIn(MIN_AGE, MAX_AGE),
        maxAge = maxAge.coerceIn(MIN_AGE, MAX_AGE),
    )

    companion object {
        const val MIN_AGE = 18
        const val MAX_AGE = 65
        const val MIN_RADIUS_KM = 1
        const val MAX_RADIUS_KM = 10_000
        const val DEFAULT_RADIUS_KM = 50

        fun oppositeGender(gender: Gender): Gender = when (gender) {
            Gender.MALE -> Gender.FEMALE
            Gender.FEMALE -> Gender.MALE
        }

        /** Пропуск онбординга: радиус «город» (1 км), противоположный пол, возраст = свой. */
        fun skipDefaults(userGender: Gender, userAgeYears: Int?): UserPreferences {
            val age = (userAgeYears ?: 25).coerceIn(MIN_AGE, MAX_AGE)
            return UserPreferences(
                preferredGenders = setOf(oppositeGender(userGender)),
                minAge = age,
                maxAge = age,
                radiusKm = 1,
            )
        }
    }
}

fun UserAccount.toPreferences(): UserPreferences {
    val genders = preferredGenders.toSet().ifEmpty { setOf(UserPreferences.oppositeGender(gender)) }
    val min = minOf(minAge, maxAge).coerceIn(UserPreferences.MIN_AGE, UserPreferences.MAX_AGE)
    val max = maxOf(minAge, maxAge).coerceIn(UserPreferences.MIN_AGE, UserPreferences.MAX_AGE)
    return UserPreferences(
        preferredGenders = genders,
        minAge = min,
        maxAge = max,
        radiusKm = radiusKm.coerceIn(UserPreferences.MIN_RADIUS_KM, UserPreferences.MAX_RADIUS_KM),
    )
}

fun UserAccount.withPreferences(prefs: UserPreferences): UserAccount = copy(
    preferredGenders = prefs.preferredGenders.toList(),
    minAge = prefs.minAge,
    maxAge = prefs.maxAge,
    radiusKm = prefs.radiusKm,
    preferencesConfigured = true,
)
