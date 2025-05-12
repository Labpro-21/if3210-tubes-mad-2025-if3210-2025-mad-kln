package com.android.purrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.ui.components.ChartCard
import com.android.purrytify.ui.components.SongCard
import com.android.purrytify.view_model.ChartViewModel
import com.android.purrytify.view_model.PlayerViewModel
import com.android.purrytify.view_model.getChartViewModel
import fetchUserId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    mediaPlayerViewModel: PlayerViewModel,
    navController: NavController,
    songRepository: SongRepository = RepositoryProvider.getSongRepository(),
    chartViewModel: ChartViewModel = getChartViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val newSongs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val recentlyPlayedSongs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val hasFetched = remember { mutableStateOf(false) }

//    val topSongViewModel: TopSongViewModel = getTopSongViewModel()
//    val topSongsGlobal by topSongViewModel.topSongsGlobal
////    val topSongsUS by topSongViewModel.topSongsUS

//    LaunchedEffect(Unit) {
//        if (topSongsGlobal.isEmpty()) topSongViewModel.fetchTopSongsGlobal()
////        if (topSongsUS.isEmpty()) topSongViewModel.fetchTopSongsUS()
//    }
//
//    LaunchedEffect(topSongsGlobal) {
//        if (topSongsGlobal.isNotEmpty()) {
//            Log.d("HomeScreen", "Fetched top songs: ${topSongsGlobal.size}")
//            recentlyPlayedSongs.value = topSongsGlobal
//        }
//    }
//
////    LaunchedEffect(topSongsUS) {
////        if (topSongsUS.isNotEmpty()) {
////            Log.d("HomeScreen", "Fetched top songs US: ${topSongsUS.size}")
////        }
////    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(200)
            val userId = fetchUserId(context)
            newSongs.value = songRepository.getNewSongsByUploader(userId, 5)
            recentlyPlayedSongs.value = songRepository.getRecentlyPlayedSongsByUploader(userId, 5)
            hasFetched.value = true
        }
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(paddingValues)
        ) {
            item {
                SongSectionHeader("Charts")
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ChartCard(
                            color = "blue",
                            title = "Top 50",
                            subtitle = "Global",
                            description ="Your daily update of the most played tracks right now - Global",
                            onClick = {
                                chartViewModel.setChartType("global")
                                navController.navigate("chart") }
                        )
                    }
                    item {
                        ChartCard(
                            color = "red",
                            title = "Top 10",
                            subtitle = "US",
                            description ="Your daily update of the most played tracks right now - US",
                            onClick = {
                                chartViewModel.setChartType("country", "US")
                                navController.navigate("chart") }
                        )
                    }
                    item {
                        ChartCard(
                            color = "green",
                            title = "Top 10",
                            subtitle = "For you",
                            description = "Your personalized chart based on your listening habits",
                            onClick = { navController.navigate("chart") }
                        )
                    }
                }
            }

            item {
                SongSectionHeader("New Songs")
            }
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newSongs.value) { song ->
                        SongCard(
                            type = "large",
                            song = song,
                            modifier = Modifier.clickable {
                                mediaPlayerViewModel.setSongs(newSongs.value)
                                mediaPlayerViewModel.playSong(context, newSongs.value.indexOf(song))
                            }
                        )
                    }
                }
            }

            item {
                SongSectionHeader("Recently Played")
            }
            items(recentlyPlayedSongs.value) { song ->
                SongCard(
                    type = "small",
                    song = song,
                    modifier = Modifier.clickable {
                        mediaPlayerViewModel.setSongs(recentlyPlayedSongs.value)
                        mediaPlayerViewModel.playSong(context, recentlyPlayedSongs.value.indexOf(song))
                    }
                )
            }
        }
    }
}

@Composable
fun SongSectionHeader(title: String) {
    Column(modifier = Modifier.padding(top = 40.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
    }
}
