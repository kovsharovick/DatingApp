package com.example.androiddatingapp.data.api

import com.example.androiddatingapp.data.api.dto.ApiResponseDto
import com.example.androiddatingapp.data.api.dto.AuthResponse
import com.example.androiddatingapp.data.api.dto.AvatarUploadResponseDto
import com.example.androiddatingapp.data.api.dto.BlockRequestDto
import com.example.androiddatingapp.data.api.dto.FeedItemDto
import com.example.androiddatingapp.data.api.dto.LoginRequest
import com.example.androiddatingapp.data.api.dto.MatchDto
import com.example.androiddatingapp.data.api.dto.MessageDto
import com.example.androiddatingapp.data.api.dto.PremiumActivationDto
import com.example.androiddatingapp.data.api.dto.RegisterRequest
import com.example.androiddatingapp.data.api.dto.ReportRequestDto
import com.example.androiddatingapp.data.api.dto.SubscriptionDto
import com.example.androiddatingapp.data.api.dto.SwipeRequestDto
import com.example.androiddatingapp.data.api.dto.UserProfileResponse
import com.example.androiddatingapp.data.api.dto.UserUpdateRequestDto
import com.example.androiddatingapp.data.api.dto.VideoResponseDto
import com.example.androiddatingapp.data.api.dto.VideoUploadResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface DatingApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("api/users/me")
    suspend fun getProfile(): UserProfileResponse

    @PUT("api/users/me")
    suspend fun updateProfile(@Body body: UserUpdateRequestDto): UserProfileResponse

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): AvatarUploadResponseDto

    @DELETE("api/users/me/avatar")
    suspend fun deleteAvatar()

    @Multipart
    @POST("api/videos")
    suspend fun uploadVideo(@Part file: MultipartBody.Part): VideoUploadResponseDto

    @GET("api/videos/me")
    suspend fun getMyVideos(): List<VideoResponseDto>

    @GET("api/cities/autocomplete")
    suspend fun searchCities(@Query("q") query: String): List<String>

    @GET("api/feed")
    suspend fun getFeed(@Query("limit") limit: Int = 20): List<FeedItemDto>

    @POST("api/swipes")
    suspend fun swipe(@Body body: SwipeRequestDto): ApiResponseDto<Boolean>

    @GET("api/matches")
    suspend fun getMatches(): List<MatchDto>

    @GET("api/messages/{matchId}/history")
    suspend fun getMessageHistory(@Path("matchId") matchId: Long): List<MessageDto>

    @GET("api/subscription")
    suspend fun getSubscription(): SubscriptionDto

    @POST("api/subscription/premium")
    suspend fun activatePremium(@Query("days") days: Int = 30): PremiumActivationDto

    @GET("api/blocks")
    suspend fun getBlockedUsers(): List<Map<String, Any>>

    @POST("api/blocks")
    suspend fun blockUser(@Body body: BlockRequestDto)

    @DELETE("api/blocks/{blockedUserId}")
    suspend fun unblockUser(@Path("blockedUserId") blockedUserId: Long)

    @POST("api/reports")
    suspend fun reportUser(@Body body: ReportRequestDto)
}
