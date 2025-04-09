package com.android.purrytify.network

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.ui.screens.LoginScreen

@Composable
fun CheckAuth(navController: NavController, context: Context) {
    var isAuthenticated by remember { mutableStateOf(false) }
    val token by TokenManager.getToken(context).collectAsState(initial = null)

    LaunchedEffect(token) {
        isAuthenticated = token.isNullOrEmpty()
    }

    if (isAuthenticated) {
        navController.navigate("home") {
            popUpTo("login") { inclusive = true }
        }
    } else {
        LoginScreen(context, navController)
    }
}
