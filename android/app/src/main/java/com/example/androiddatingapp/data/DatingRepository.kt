package com.example.androiddatingapp.data

import android.content.Context
import android.net.Uri
import com.example.androiddatingapp.BuildConfig
import com.example.androiddatingapp.data.api.ApiClient
import com.example.androiddatingapp.data.api.DatingApiService
import com.example.androiddatingapp.data.api.dto.FeedItemDto
import com.example.androiddatingapp.data.api.dto.MatchDto
import com.example.androiddatingapp.data.api.dto.MessageDto
import com.example.androiddatingapp.data.api.dto.SubscriptionDto
import com.example.androiddatingapp.data.api.dto.SwipeDirectionDto
import com.example.androiddatingapp.data.api.dto.SwipeRequestDto
import com.example.androiddatingapp.data.api.dto.UserProfileResponse
import com.example.androiddatingapp.data.api.dto.UserUpdateRequestDto
import com.example.androiddatingapp.ui.model.Gender
import com.example.androiddatingapp.ui.model.ProfileUi
import com.example.androiddatingapp.ui.model.UserAccount
import com.example.androiddatingapp.ui.util.DateOfBirthInput
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DatingRepository(
    private val api: DatingApiService = ApiClient.api,
) {

    /** Только ответ API — для регистрации (имя должно совпадать с БД сервера). */
    suspend fun searchCitiesFromServer(query: String): Result<List<String>> = runCatching {
        api.searchCities(CityStrings.normalize(query))
            .map { CityStrings.normalize(it) }
            .distinct()
    }.mapApiError()

    /** Подсказки: API, при пустом ответе или ошибке сети — локальный справочник. */
    suspend fun searchCities(context: Context, query: String): Result<List<String>> = runCatching {
        val trimmed = CityStrings.normalize(query)
        if (trimmed.isEmpty()) return@runCatching emptyList()

        var networkError: Throwable? = null
        val remote = runCatching { api.searchCities(trimmed) }
            .getOrElse {
                networkError = it
                emptyList()
            }
            .map { CityStrings.normalize(it) }

        if (remote.isNotEmpty()) {
            return@runCatching remote.distinct()
        }

        val local = LocalCityIndex.search(context, trimmed)
        if (local.isNotEmpty()) {
            return@runCatching local
        }

        when {
            networkError != null -> throw IllegalStateException(
                "Не удалось связаться с сервером (${BuildConfig.API_BASE_URL}). " +
                    "Запустите Spring Boot на компьютере.",
                networkError,
            )
            else -> throw IllegalStateException(
                "Справочник городов на сервере пуст. " +
                    "Импортируйте таблицу city в PostgreSQL (файл datinfAppPlain.sql в корне проекта).",
            )
        }
    }.mapApiError()

    suspend fun resolveCityForRegistration(cityInput: String): Result<String> = runCatching {
        val input = CityStrings.normalize(cityInput)
        if (input.isEmpty()) {
            throw IllegalArgumentException("Введите город")
        }
        val matches = api.searchCities(input).map { CityStrings.normalize(it) }.distinct()
        if (matches.isEmpty()) {
            throw IllegalStateException(
                "Справочник городов на сервере пуст. " +
                    "Импортируйте datinfAppPlain.sql в PostgreSQL, затем перезапустите бэкенд.",
            )
        }
        matches.firstOrNull { it.equals(input, ignoreCase = true) }
            ?: matches.firstOrNull()
            ?: throw IllegalArgumentException(
                "Город «$input» не найден на сервере. Выберите город из списка подсказок.",
            )
    }.mapApiError()

    suspend fun getFeed(limit: Int = 20): Result<List<ProfileUi>> = runCatching {
        api.getFeed(limit).map { it.toProfileUi() }
    }.mapApiError()

    suspend fun swipe(targetUserId: Long, like: Boolean): Result<Boolean> = runCatching {
        val direction = if (like) SwipeDirectionDto.LIKE else SwipeDirectionDto.DISLIKE
        api.swipe(SwipeRequestDto(targetUserId = targetUserId, direction = direction)).data == true
    }.mapApiError()

    suspend fun getMatches(): Result<List<MatchDto>> = runCatching {
        api.getMatches()
    }.mapApiError()

    suspend fun getMessageHistory(matchId: Long): Result<List<MessageDto>> = runCatching {
        api.getMessageHistory(matchId)
    }.mapApiError()

    suspend fun getProfile(): Result<UserProfileResponse> = runCatching {
        api.getProfile()
    }.mapApiError()

    suspend fun updateProfile(request: UserUpdateRequestDto): Result<UserProfileResponse> = runCatching {
        api.updateProfile(request)
    }.mapApiError()

    suspend fun updateProfileFromAccount(
        email: String,
        account: UserAccount,
        dateOfBirth: String = account.dateOfBirth,
        gender: Gender = account.gender,
    ): Result<UserAccount> = runCatching {
        val apiDate = DateOfBirthInput.toApiIsoDate(dateOfBirth.trim())
        val response = api.updateProfile(
            UserUpdateRequestDto(
                name = account.name.trim().ifBlank { null },
                dateOfBirth = apiDate,
                gender = gender.name,
                city = account.city.trim().ifBlank { null },
                description = account.description.trim().ifBlank { null },
                hidden = !account.isProfileActive,
            ),
        )
        account.mergeProfile(response, email)
    }.mapApiError()

    suspend fun setProfileHidden(hidden: Boolean): Result<UserProfileResponse> = runCatching {
        api.updateProfile(UserUpdateRequestDto(hidden = hidden))
    }.mapApiError()

    suspend fun getSubscription(): Result<SubscriptionDto> = runCatching {
        api.getSubscription()
    }.mapApiError()

    suspend fun activatePremium(days: Int = 30): Result<String> = runCatching {
        api.activatePremium(days).message
    }.mapApiError()

    suspend fun uploadVideo(context: Context, uri: Uri): Result<UserProfileResponse> = runCatching {
        api.uploadVideo(MediaUpload.createVideoPart(context, uri))
        api.getProfile()
    }.mapApiError()

    suspend fun uploadAvatar(context: Context, uri: Uri): Result<UserProfileResponse> = runCatching {
        api.uploadAvatar(MediaUpload.createImagePart(context, uri))
        api.getProfile()
    }.mapApiError()

    suspend fun deleteAvatar(): Result<UserProfileResponse> = runCatching {
        api.deleteAvatar()
        api.getProfile()
    }.mapApiError()

    suspend fun refreshAccount(email: String, account: UserAccount): Result<UserAccount> = runCatching {
        account.mergeProfile(api.getProfile(), email)
    }.mapApiError()

    suspend fun blockUser(userId: Long): Result<Unit> = runCatching {
        api.blockUser(com.example.androiddatingapp.data.api.dto.BlockRequestDto(userId))
    }.mapApiError()

    suspend fun reportUser(userId: Long, reason: String): Result<Unit> = runCatching {
        api.reportUser(
            com.example.androiddatingapp.data.api.dto.ReportRequestDto(
                reportedUserId = userId,
                reason = reason,
            ),
        )
    }.mapApiError()

    private fun FeedItemDto.toProfileUi(): ProfileUi = ProfileUi(
        userId = userId,
        name = name,
        age = age,
        city = listOfNotNull(city, region?.takeIf { it.isNotBlank() })
            .joinToString(", "),
        description = description.orEmpty(),
        videoUrl = videoUrl.orEmpty(),
        thumbnailUrl = thumbnailUrl.orEmpty(),
        avatarUrl = avatarUrl.orEmpty(),
    )

    companion object {
        fun UserAccount.mergeProfile(profile: UserProfileResponse, email: String): UserAccount = copy(
            userId = profile.id,
            email = email,
            name = profile.name,
            city = profile.city,
            description = profile.description.orEmpty(),
            hasVideo = !profile.videoUrl.isNullOrBlank(),
            videoUrl = profile.videoUrl.orEmpty(),
            videoTitle = videoFileLabel(profile.videoUrl),
            avatarUrl = profile.avatarUrl.orEmpty(),
            isProfileActive = !profile.hidden,
        )

        fun matchesToNotifications(matches: List<MatchDto>): List<MatchNotification> =
            matches.map { match ->
                MatchNotification(
                    id = "match_${match.matchId}",
                    title = "Совпадение",
                    body = "У вас совпадение с ${match.partnerName}",
                    time = formatDateTime(match.matchedAt),
                    matchId = match.matchId,
                )
            }

        internal fun videoFileLabel(videoUrl: String?): String =
            if (videoUrl.isNullOrBlank()) "" else "profile_video.mp4"

        fun formatDateTime(iso: String?): String {
            if (iso.isNullOrBlank()) return ""
            return try {
                val normalized = if (iso.length >= 19) iso.substring(0, 19) else iso
                val dt = LocalDateTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                dt.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (_: DateTimeParseException) {
                iso.take(10)
            }
        }

        fun accountWithLikesRemaining(account: UserAccount, likesRemaining: Int): UserAccount {
            val today = java.time.LocalDate.now().toString()
            return if (likesRemaining <= UserAccount.DAILY_FREE_LIKES) {
                account.copy(
                    likeQuotaDay = today,
                    likesUsedToday = UserAccount.DAILY_FREE_LIKES - likesRemaining,
                    bonusLikes = 0,
                )
            } else {
                account.copy(
                    likeQuotaDay = today,
                    likesUsedToday = 0,
                    bonusLikes = likesRemaining - UserAccount.DAILY_FREE_LIKES,
                )
            }
        }
    }
}

data class MatchNotification(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val matchId: Long,
)

private fun <T> Result<T>.mapApiError(): Result<T> =
    exceptionOrNull()?.let { Result.failure(Exception(ApiClient.parseErrorMessage(it))) }
        ?: this
