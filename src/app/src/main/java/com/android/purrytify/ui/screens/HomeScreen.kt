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
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.ui.components.SongCard
import com.android.purrytify.ui.components.SongCardFake
import com.android.purrytify.ui.components.SongCardFakeProps
import com.android.purrytify.view_model.PlayerViewModel
import fetchUserId
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    mediaPlayerViewModel: PlayerViewModel,
    songRepository: SongRepository = RepositoryProvider.getSongRepository()
) {
    val recentlyPlayedSongs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val newSongs = remember { mutableStateOf<List<Song>>(emptyList()) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    fun fetchUser() {
        coroutineScope.launch {
            val userId = fetchUserId(context)
            newSongs.value = songRepository.getNewSongsByUploader(userId, 5)
            recentlyPlayedSongs.value = songRepository.getRecentlyPlayedSongsByUploader(userId, 5)
        }
    }

    LaunchedEffect(Unit) {
        fetchUser()
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
                Column(
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Text(
                        "New Songs",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
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
                                    mediaPlayerViewModel.playSong(context, index = newSongs.value.indexOf(song))
                                })
                        }
                    }
                }
            }


            item {
                Text(
                    "Recently Played",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(recentlyPlayedSongs.value) { song ->
                SongCard(type = "small",
                    song = song,
                    modifier = Modifier.clickable {
                        mediaPlayerViewModel.setSongs(recentlyPlayedSongs.value)
                        mediaPlayerViewModel.playSong(context, index = recentlyPlayedSongs.value.indexOf(song))
                })

            }
        }
    }
}