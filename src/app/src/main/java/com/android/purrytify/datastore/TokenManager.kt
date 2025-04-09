package com.android.purrytify.datastore

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.purrytify.network.RefreshToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object TokenManager {
    private val Context.dataStore by preferencesDataStore("auth_prefs")
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    private val USER_ID = intPreferencesKey("user_id")

    suspend fun saveToken(context: Context, accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = accessToken
            prefs[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveId(context: Context, id: Int ){
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN]
        }
    }

    fun getRefreshToken(context: Context): Flow<String?>{
        return context.dataStore.data.map { prefs ->
            prefs[REFRESH_TOKEN]
        }
    }

    fun getCurrentId(context: Context): Flow<Int?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID]
        }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(USER_ID)
        }
    }
}

