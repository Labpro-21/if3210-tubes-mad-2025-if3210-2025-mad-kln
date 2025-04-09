package com.android.purrytify.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshToken(
    val refreshToken: String
)
data class ProfileResponse(
    val id: Int,
    val username: String,
    val email: String,
    val profilePhoto: String,
    val location: String,
    val createdAt: String,
    val updatedAt: String
)

data class Verify(
    val id: Int,
    val username: String
)

data class VerifyResponse(
    val valid: Boolean,
    val user: Verify
)

interface ApiService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("/api/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    @POST("/api/refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshToken
    ): LoginResponse

    @GET("/api/verify-token")
    suspend fun verifyToken(
        @Header("Authorization") token: String
    ): VerifyResponse
}

