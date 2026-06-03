package com.example.androiddatingapp.data.api.dto

data class FeedItemDto(
    val userId: Long,
    val name: String,
    val age: Int,
    val city: String,
    val region: String? = null,
    val videoUrl: String? = null,
    val description: String? = null,
    val likedYou: Boolean = false,
    val avatarUrl: String? = null,
    val thumbnailUrl: String? = null,
)

enum class SwipeDirectionDto {
    LIKE,
    DISLIKE,
}

data class SwipeRequestDto(
    val targetUserId: Long,
    val direction: SwipeDirectionDto,
)

data class ApiResponseDto<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
)

data class MatchDto(
    val matchId: Long,
    val partnerId: Long,
    val partnerName: String,
    val partnerAge: Int,
    val partnerCity: String,
    val matchedAt: String? = null,
    val lastMessagePreview: String? = null,
    val partnerAvatarUrl: String? = null,
)

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val content: String,
    val sentAt: String? = null,
)

data class UserUpdateRequestDto(
    val name: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val city: String? = null,
    val description: String? = null,
    val hidden: Boolean? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val radiusKm: Int? = null,
    val preferredGenders: List<String>? = null,
)

data class SubscriptionDto(
    val plan: String,
    val effectivePlan: String,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val likesRemaining: Int = 0,
)

data class BlockRequestDto(
    val blockedUserId: Long,
)

data class ReportRequestDto(
    val reportedUserId: Long,
    val reason: String,
)

data class PremiumActivationDto(
    val message: String,
)
