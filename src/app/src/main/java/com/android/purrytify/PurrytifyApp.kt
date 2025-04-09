package com.android.purrytify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import android.content.Context
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.ui.modal.MiniPlayer
import com.android.purrytify.ui.screens.NowPlayingScreen
import com.android.purrytify.view_model.PlayerViewModel

@Composable
fun PurrytifyApp(context: Context) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val token by TokenManager.getToken(context).collectAsState(initial = null)
    val mediaPlayerViewModel: PlayerViewModel = viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentSong by mediaPlayerViewModel.currentSong.collectAsState()

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
        Box(
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {

            NavHost(
                navController = navController,
                startDestination = "login",
            ) {
                composable("login") {
                    LoginScreen(context, navController)
                }
                composable("home") {
                    HomeScreen(
                        mediaPlayerViewModel = mediaPlayerViewModel,
                    )
                }
                composable("library") {
                    LibraryScreen(
                        mediaPlayerViewModel = mediaPlayerViewModel,
                    )
                }
                composable("nowPlaying") {
                    NowPlayingScreen(
                        viewModel = mediaPlayerViewModel,
                        onClose = { navController.popBackStack() }
                    )
                }
            }
            if (currentRoute != "nowPlaying") {
                AnimatedVisibility(
                    visible = currentSong != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                ) {
                    MiniPlayer(
                        viewModel = mediaPlayerViewModel,
                        onOpenFullPlayer = { navController.navigate("nowPlaying") },
                    )
                }
            }
        }
    }
}
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

