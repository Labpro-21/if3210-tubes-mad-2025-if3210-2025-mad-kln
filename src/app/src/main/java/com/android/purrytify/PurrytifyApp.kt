package com.android.purrytify

import android.content.Context
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.android.purrytify.ui.components.BottomNavbar
import com.android.purrytify.ui.screens.HomeScreen
import com.android.purrytify.ui.screens.LibraryScreen
import com.android.purrytify.ui.screens.LoginScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.ui.screens.NowPlayingScreen

@Composable
fun PurrytifyApp(songRepository: SongRepository, context: Context) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val token by TokenManager.getTokenFlow(context).collectAsState(initial = null)

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    LaunchedEffect(token) {
        systemUiController.isStatusBarVisible = false
        if (token.isNullOrEmpty()) {
            navController.navigate("login") {
                popUpTo(0)
            }
        } else {
            navController.navigate("home") {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != "splash") {
                BottomNavbar(navController)
            }
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable("login") { LoginScreen(context, navController) }
            composable("home") { HomeScreen() }
            composable("library") { LibraryScreen(songRepository) }
            composable("profile") { NowPlayingScreen { navController.popBackStack() } }
        }
    }
}

@Composable
fun CheckAuth(navController: NavController, context: Context) {
    var isAuthenticated by remember { mutableStateOf(false) }
    val token by TokenManager.getTokenFlow(context).collectAsState(initial = null)

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

