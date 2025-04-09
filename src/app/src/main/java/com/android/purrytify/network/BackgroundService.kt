package com.android.purrytify.network

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.android.purrytify.datastore.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull


suspend fun checkToken(context: Context) {
    val token = TokenManager.getToken(context).firstOrNull()
    val refresh_Token = TokenManager.getRefreshToken(context).firstOrNull()

    if (token.isNullOrEmpty() || refresh_Token.isNullOrEmpty()) {
        Log.d("CHECK_TOKEN", "Token or Refresh Token is null/empty")
        return
    }

    try {
        val bearerToken = "Bearer $token"
        val verifyResponse = RetrofitClient.api.verifyToken(bearerToken)
        if (verifyResponse.valid) {
            Log.d("VERIFY_TOKEN", "Token is valid ✅")
        }
    } catch (e: retrofit2.HttpException) {
        if (e.code() == 401) {
            Log.d("VERIFY_TOKEN", "Token invalid (401), refreshing...")
            try {
                refreshToken(context, refresh_Token)
            } catch (refreshErr: Exception) {
                Log.e("REFRESH_TOKEN", "Failed to refresh token: ${refreshErr.message}")
            }
        } else {
            Log.e("VERIFY_TOKEN", "Unexpected HTTP error: ${e.code()} - ${e.message()}")
        }
    } catch (e: Exception) {
        Log.e("VERIFY_TOKEN", "Unexpected error: ${e.message}")
    }
}

suspend fun refreshToken(context: Context, refreshToken: String) {
    try {
        val response = RetrofitClient.api.refreshToken(RefreshToken(refreshToken))
        TokenManager.saveToken(context, response.accessToken, response.refreshToken)
        Log.d("REFRESH_TOKEN", "Token refreshed successfully ✅")
    } catch (e: Exception) {
        Log.e("REFRESH_TOKEN", "Refresh token failed : ${e.message}")
    }
}
