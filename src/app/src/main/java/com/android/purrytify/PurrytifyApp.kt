package com.android.purrytify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.network.checkToken
import com.android.purrytify.ui.modal.MiniPlayer
import com.android.purrytify.ui.screens.NowPlayingScreen
import com.android.purrytify.ui.screens.ProfileScreen
import com.android.purrytify.view_model.getPlayerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.android.purrytify.ui.components.NetworkStatus
import com.android.purrytify.network.NetworkMonitor
import com.android.purrytify.ui.screens.NoInternetScreen


@Composable
fun PurrytifyApp(context: Context) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val token by TokenManager.getToken(context).collectAsState(initial = null)
    val mediaPlayerViewModel = getPlayerViewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentSong by mediaPlayerViewModel.currentSong.collectAsState()

    val networkMonitor = remember {NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState()

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(5 * 60 * 1000)
                checkToken(context)
            }
        }

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
                composable("profile"){
                    if (isConnected){
                        ProfileScreen(navController)
                    } else {
                        NoInternetScreen()
                    }
                }
            }
            if (currentRoute != "profile"){
                NetworkStatus(networkMonitor)
            }
            if (currentRoute != "nowPlaying") {
                AnimatedVisibility(
                    visible = currentSong != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 72.dp)
                ) {
                    MiniPlayer(
                        viewModel = mediaPlayerViewModel,
                        onOpenFullPlayer = { navController.navigate("nowPlaying") },
                    )
                }
            }

            if (currentRoute != "login") {
                BottomNavbar(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                )
            }
        }
    }
}
