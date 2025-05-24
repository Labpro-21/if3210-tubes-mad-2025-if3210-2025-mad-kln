package com.android.purrytify

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.network.NetworkMonitor
import com.android.purrytify.network.checkToken
import com.android.purrytify.ui.components.BottomNavbar
import com.android.purrytify.ui.components.NetworkStatus
import com.android.purrytify.ui.components.SideNavbar
import com.android.purrytify.ui.modal.MiniPlayer
import com.android.purrytify.ui.screens.*
import com.android.purrytify.view_model.getPlayerViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

@Composable
fun PurrytifyApp(context: Context, intent: Intent) {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()
    val mediaPlayerViewModel = getPlayerViewModel()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentSong by mediaPlayerViewModel.currentSong.collectAsState()

    val networkMonitor = remember { NetworkMonitor(context) }
    val isConnected by networkMonitor.isConnected.collectAsState()

    val token by TokenManager.getToken(context).collectAsState(initial = null)
    var hasLoaded by remember { mutableStateOf(false) }

    val deepLinkedSong by SharedSongState.deepLinkedSong.collectAsState()
    var lastMainRoute by rememberSaveable { mutableStateOf<String?>(null) }

    val configuration = LocalConfiguration.current

    LaunchedEffect(deepLinkedSong) {
        deepLinkedSong?.let { song ->
            val songList = listOf(song)
            mediaPlayerViewModel.setSongs(songList)
            mediaPlayerViewModel.playSong(context, 0)

            delay(1000)
            if (currentRoute != null && currentRoute != "login" && currentRoute != "blank" && currentRoute != "nowPlaying") {
                lastMainRoute = currentRoute
            }
            
            navController.navigate("nowPlaying") {
                launchSingleTop = true
            }
            SharedSongState.clear()
        }
    }

    LaunchedEffect(token) {
        if (hasLoaded && token != null) {
            if (token!!.isEmpty()) {
                navController.navigate("login") {
                    popUpTo(0)
                }
            } else {
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }
    }

    LaunchedEffect(currentRoute) {
        Log.d("CURRENT ROUTE",currentRoute ?: "NULL")
        if (!currentRoute.isNullOrEmpty() &&
            currentRoute != "nowPlaying" &&
            currentRoute != "blank" &&
            currentRoute != "login") {
            lastMainRoute = currentRoute
        }
    }

    LaunchedEffect(Unit) {
        mediaPlayerViewModel.initialize(context)

        checkToken(context)
        delay(100)
        if (token.isNullOrEmpty()) {
            navController.navigate("login") {
                popUpTo(0)
            }
        } else {
            if (intent.getBooleanExtra("open_now_playing", false)) {
                Log.d("LAST ROUTE",lastMainRoute ?: "NULL")
                val routeToRestore = lastMainRoute ?: "home"
                navController.navigate(routeToRestore) {
                    popUpTo(0)
                    launchSingleTop = true
                }
                navController.navigate("nowPlaying") {
                    launchSingleTop = true
                }
            } else {
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }

        hasLoaded = true
        systemUiController.isStatusBarVisible = false


        while (true) {
            delay(5 * 60 * 1000)
            checkToken(context)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Row(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (currentRoute != "login" && currentRoute != "blank") {
                    SideNavbar(
                        navController = navController,
                        mediaPlayerViewModel = mediaPlayerViewModel,
                        currentSong = currentSong,
                        onOpenFullPlayer = { navController.navigate("nowPlaying") },
                        currentRoute = currentRoute,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    NavHost(
                        navController = navController,
                        startDestination = "blank",
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable("blank") {
                            BlankScreen()
                        }
                        composable("login") {
                            LoginScreen(context, navController)
                        }
                        composable("home") {
                            if (!token.isNullOrEmpty()) {
                                HomeScreen(mediaPlayerViewModel = mediaPlayerViewModel, navController = navController)
                            } else {
                                BlankScreen()
                            }
                        }
                        composable("library") {
                            LibraryScreen(mediaPlayerViewModel = mediaPlayerViewModel)
                        }
                        composable("nowPlaying") {
                            NowPlayingScreen(
                                viewModel = mediaPlayerViewModel,
                                onClose = { navController.popBackStack() }
                            )
                        }
                        composable("chart") {
                            if (isConnected) {
                                ChartScreen(onClose = { navController.popBackStack() })
                            } else {
                                NoInternetScreen()
                            }
                        }
                        composable("profile") {
                            if (isConnected) {
                                ProfileScreen(navController)
                            } else {
                                NoInternetScreen()
                            }
                        }
                        composable("qr_scanner") {
                            QRScannerScreen(navController)
                        }
                    }
                     if (currentRoute != "profile") {
                        NetworkStatus(networkMonitor)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "blank",
                ) {
                    composable("blank") {
                        BlankScreen()
                    }
                    composable("login") {
                        LoginScreen(context, navController)
                    }
                    composable("home") {
                        if (!token.isNullOrEmpty()) {
                            HomeScreen(mediaPlayerViewModel = mediaPlayerViewModel, navController = navController)
                        } else {
                            BlankScreen()
                        }
                    }
                    composable("library") {
                        LibraryScreen(mediaPlayerViewModel = mediaPlayerViewModel)
                    }
                    composable("nowPlaying") {
                        NowPlayingScreen(
                            viewModel = mediaPlayerViewModel,
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable("chart") {
                        if (isConnected) {
                            ChartScreen(onClose = { navController.popBackStack() })
                        } else {
                            NoInternetScreen()
                        }
                    }
                    composable("profile") {
                        if (isConnected) {
                            ProfileScreen(navController)
                        } else {
                            NoInternetScreen()
                        }
                    }
                    composable("qr_scanner") {
                        QRScannerScreen(navController)
                    }
                }

                if (currentRoute != "profile") {
                    NetworkStatus(networkMonitor)
                }

                if (currentRoute != "nowPlaying" && currentRoute != "login" && currentRoute != "blank") {
                    AnimatedVisibility(
                        visible = currentSong != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                    ) {
                        MiniPlayer(
                            viewModel = mediaPlayerViewModel,
                            onOpenFullPlayer = { navController.navigate("nowPlaying") },
                        )
                    }
                }

                if (currentRoute != "login" && currentRoute != "blank") {
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
}
