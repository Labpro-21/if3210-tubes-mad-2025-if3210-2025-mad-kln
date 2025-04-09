package com.android.purrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.ui.components.SongCard
import com.android.purrytify.ui.components.SongCardFake
import com.android.purrytify.ui.components.SongCardFakeProps
import kotlinx.coroutines.launch
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.ui.adapter.SongAdapter
import com.android.purrytify.ui.modal.SongUploadModal
import com.android.purrytify.view_model.PlayerViewModel



@Composable
fun LibraryScreen(
    mediaPlayerViewModel: PlayerViewModel,
) {
    val songRepository = RepositoryProvider.getSongRepository()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val songTab = remember { mutableStateOf("all") }

    val allSongs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val likedSongs = remember { mutableStateOf<List<Song>>(emptyList()) }

    val recyclerViewRef = remember { mutableStateOf<RecyclerView?>(null) }
    val songAdapterRef = remember { mutableStateOf<SongAdapter?>(null) }

    val activeSongs by remember(allSongs.value, likedSongs.value, songTab.value) {
        derivedStateOf {
            if (songTab.value == "all") allSongs.value else likedSongs.value
        }
    }

    fun fetchSongs() {
        coroutineScope.launch {
            allSongs.value = songRepository.getAllSongs() ?: emptyList()
            likedSongs.value = songRepository.getLikedSongsByUploader(1) ?: emptyList()
        }
    }

    LaunchedEffect(Unit) {
        fetchSongs()
    }

    LaunchedEffect(activeSongs) {
        Log.d("LibraryScreen", "Active Songs Count: ${activeSongs.size}")
        mediaPlayerViewModel.setSongs(activeSongs)
//        songAdapterRef.value?.updateSongs(activeSongs)
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PageHeader({ fetchSongs() })

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StyledButton(
                    text = "All",
                    isSelected = songTab.value == "all",
                    onClick = {
                        songTab.value = "all"
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                StyledButton(
                    text = "Liked",
                    isSelected = songTab.value == "liked",
                    onClick = {
                        songTab.value = "liked"
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.Gray
            )

//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color(0xFF121212))
//                    .padding(paddingValues)
//            ) {
//                items(activeSongs) { song ->
//                    SongCard(
//                        type = "small",
//                        song = song,
//                        modifier = Modifier.clickable {
//                            mediaPlayerViewModel.playSong(context, index = activeSongs.indexOf(song))
//                            Log.d("LibraryScreen", "Playing Song: ${song.title} - ${song.artist}")
//                        }
//                    )
//                }
//            }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = SongAdapter(activeSongs) { song ->
                            val index = activeSongs.indexOf(song)
                            mediaPlayerViewModel.playSong(context, index)
                            Log.d("LibraryScreen", "Playing Song: ${song.title} - ${song.artist}")
                        }.also { songAdapterRef.value = it }
                        recyclerViewRef.value = this
                        setBackgroundColor(Color(0xFF121212).toArgb())
                    }
                },
                update = { recyclerView ->
                    songAdapterRef.value?.updateSongs(activeSongs)
                }
            )

        }
    }
}

@Composable
fun PageHeader(func: () -> Unit) {
    Row (modifier = Modifier
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom) {
        Text(
            "Your Library",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp, top = 40.dp, bottom = 8.dp)
        )
        SongUploadButton(func)
    }
}

@Composable
fun StyledButton(
    text: String,
    isSelected: Boolean,
    onClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) Color(0xFF1DB954) else Color(0xFF333333)
    val textColor = if (isSelected) Color.Black else Color.White

    Button(
        onClick = { onClick?.invoke() },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier
            .height(35.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            modifier =  Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
fun SongUploadButton(func: () -> Unit) {

    val isModalVisible = remember { mutableStateOf(false) }
    Button(
        onClick = { isModalVisible.value = true },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = Modifier
            .padding(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = "+",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.Light,
        )
    }

    SongUploadModal(
        isVisible = isModalVisible.value,
        onDismiss = { refresh ->
            isModalVisible.value = false
            if (refresh) {
                Log.d("LibraryScreen", "Modal closed: Refreshing songs")
                func()
            } else {
                Log.d("LibraryScreen", "Modal closed: Not refreshing songs")
            }
        },
    )
}
