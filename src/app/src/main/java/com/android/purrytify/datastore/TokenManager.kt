package com.android.purrytify.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object TokenManager {
    private val Context.dataStore by preferencesDataStore("auth_prefs")
    private val ACCESS_TOKEN = stringPreferencesKey("access_token")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[ACCESS_TOKEN]
        }
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
        }
    }
}

