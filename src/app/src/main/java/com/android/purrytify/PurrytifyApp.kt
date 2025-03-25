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
import com.android.purrytify.ui.screens.ProfileScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.LaunchedEffect

@Composable
fun PurrytifyApp() {
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
            startDestination = "home",
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen() }
            composable("library") { LibraryScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}
