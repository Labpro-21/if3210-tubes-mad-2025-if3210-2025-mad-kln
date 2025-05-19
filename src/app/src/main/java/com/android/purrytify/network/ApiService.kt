package com.android.purrytify.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.io.File

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

data class OnlineSongResponse(
    val id: Int,
    val title: String,
    val artist: String,
    val artwork: String,
    val url: String,
    val duration: String,
    val country: String,
    val rank: Int,
    val createdAt: String,
    val updatedAt: String,
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

    @GET("/api/top-songs/global")
    suspend fun getTopSongsGlobal(): List<OnlineSongResponse>

    @GET("/api/top-songs/{country}")
    suspend fun getTopSongsCountry(
        @Path("country") country: String
    ): List<OnlineSongResponse>

    @GET("/api/songs/{song_id}")
    suspend fun getSongById(
        @Path("song_id") songId: Int
    ): OnlineSongResponse

    @Multipart
    @PATCH("api/profile")
    suspend fun editProfile(
        @Header ("Authorization") token: String,
        @Part location: MultipartBody.Part?,
        @Part profilePhoto: MultipartBody.Part?
    )

}
fun prepareMultipart(
    location: String?,
    profileImageFile: File?
): Pair<MultipartBody.Part?, MultipartBody.Part?> {
    val locationPart = location?.let {
        MultipartBody.Part.createFormData("location", it)
    }

    val imagePart = profileImageFile?.let {
        val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData("profilePhoto", it.name, requestFile)
    }

    return locationPart to imagePart
}


