package com.example.androiddatingapp.data.api

import com.example.androiddatingapp.data.api.dto.AuthResponse
import com.example.androiddatingapp.data.api.dto.LoginRequest
import com.example.androiddatingapp.data.api.dto.RegisterRequest
import com.example.androiddatingapp.data.api.dto.UserProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface DatingApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("api/users/me")
    suspend fun getProfile(): UserProfileResponse

    @GET("api/cities")
    suspend fun searchCities(@Query("prefix") prefix: String): List<String>
}
