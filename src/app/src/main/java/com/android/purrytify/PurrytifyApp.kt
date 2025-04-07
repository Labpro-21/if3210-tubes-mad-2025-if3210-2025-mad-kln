package com.android.purrytify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.ui.modal.MiniPlayer
import com.android.purrytify.ui.screens.NowPlayingScreen
import com.android.purrytify.view_model.PlayerViewModel

@Composable
fun PurrytifyApp(songRepository: SongRepository) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val mediaPlayerViewModel: PlayerViewModel = viewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentSong by mediaPlayerViewModel.currentSong.collectAsState()

    LaunchedEffect(Unit) {
        systemUiController.isStatusBarVisible = false
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
                startDestination = "home",
            ) {
                composable("login") {
                    LoginScreen()
                }
                composable("home") {
                    HomeScreen(
                        songRepository = songRepository,
                        mediaPlayerViewModel = mediaPlayerViewModel,
                        onMiniplayerClick = { navController.navigate("nowPlaying") }
                    )
                }
                composable("library") {
                    LibraryScreen(
                        songRepository = songRepository,
                        mediaPlayerViewModel = mediaPlayerViewModel,
                        onMiniplayerClick = { navController.navigate("nowPlaying") }
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

