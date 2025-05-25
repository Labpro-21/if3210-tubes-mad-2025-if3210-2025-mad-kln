package com.android.purrytify.ui.screens

import android.util.Log
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
import getToken
import com.android.purrytify.network.RetrofitClient
import com.android.purrytify.network.checkToken
import com.android.purrytify.ui.components.ChartCard
import com.android.purrytify.ui.components.SongCard
import com.android.purrytify.view_model.ChartViewModel
import com.android.purrytify.view_model.PlayerViewModel
import com.android.purrytify.view_model.getChartViewModel
import fetchUserId
import kotlinx.coroutines.coroutineScope
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
    val userLocationState = remember { mutableStateOf("") }
    val userIdState = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val fetchedUserId = fetchUserId(context)
            userIdState.value = fetchedUserId
            chartViewModel.setUserId(fetchedUserId)

            try {
                checkToken(context)
                val bearerToken = "Bearer ${getToken(context)}"
                val user = RetrofitClient.api.getProfile(bearerToken)
                userLocationState.value = user.location
            } catch (e: Exception) {
                userLocationState.value = ""
            }

            if (userIdState.value != 0) {
                Log.d("HomeScreen", "Fetching datas")
                chartViewModel.fetchAllChartsInitial(userLocationState.value)
                newSongs.value = songRepository.getNewSongsByUploader(userIdState.value, 5)
                recentlyPlayedSongs.value = songRepository.getRecentlyPlayedSongsByUploader(userIdState.value, 5)

                for (song in recentlyPlayedSongs.value) {
                    Log.d("HomeScreen", "Recently Played Song: ${song.title}")
                }
            }
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
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 72.dp)
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
                                navController.navigate("chart")
                            }
                        )
                    }
                    item {
                        ChartCard(
                            color = "red",
                            title = "Top 10",
                            subtitle = userLocationState.value.ifEmpty { "Country" },
                            description = "Your daily update of the most played tracks right now - ${userLocationState.value.ifEmpty { "your country" }}",
                            onClick = {
                                chartViewModel.setChartType("country", userLocationState.value)
                                navController.navigate("chart")
                            }
                        )
                    }
                    item {
                        ChartCard(
                            color = "green",
                            title = "For You",
                            subtitle = "Personalized",
                            description = "Your personalized chart based on your listening habits",
                            onClick = {
                                chartViewModel.setChartType("foryou", userLocationState.value)
                                navController.navigate("chart")
                            }
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
            item {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    items(recentlyPlayedSongs.value) { song ->
                        SongCard(
                            type = "small",
                            song = song,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .fillMaxWidth()
                                .clickable {
                                    mediaPlayerViewModel.setSongs(recentlyPlayedSongs.value)
                                    mediaPlayerViewModel.playSong(context, recentlyPlayedSongs.value.indexOf(song))
                                }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
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
