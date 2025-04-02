package com.android.purrytify

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
import com.android.purrytify.ui.screens.ProfileScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.LaunchedEffect
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.ui.screens.NowPlayingScreen

@Composable
fun PurrytifyApp(songRepository: SongRepository) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()

    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = false
    }

    Scaffold(
        bottomBar = { BottomNavbar(navController) },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable("login") { LoginScreen() }
            composable("home") { HomeScreen() }
            composable("library") { LibraryScreen(songRepository) }
            composable("profile") { NowPlayingScreen {navController.popBackStack()} }
        }
    }
}