package com.android.purrytify

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.android.purrytify.ui.components.BottomNavbar
import com.android.purrytify.ui.screens.HomeScreen
import com.android.purrytify.ui.screens.LibraryScreen
import com.android.purrytify.ui.screens.ProfileScreen

@Composable
fun PurrytifyApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavbar(navController) }
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
