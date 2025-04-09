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
import androidx.compose.ui.platform.LocalContext
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.ui.modal.SongUploadModal
import com.android.purrytify.view_model.PlayerViewModel

@Composable
fun LibraryScreen(
    mediaPlayerViewModel: PlayerViewModel,
) {
    val songRepository = RepositoryProvider.getSongRepository()

    val isAllSelected = remember { mutableStateOf(true) }
    val isLikedSelected = remember { mutableStateOf(false) }

    val allSongs = remember { mutableStateOf<List<Song>>(emptyList()) }
    val likedSongs = remember { mutableStateOf<List<Song>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun fetchSongs() {
        coroutineScope.launch {
            allSongs.value = songRepository.getAllSongs() ?: emptyList()
            likedSongs.value = songRepository.getLikedSongsByUploader(1) ?: emptyList()
        }
    }

    fun getActiveSongs(): List<Song> {
        return if (isAllSelected.value) {
            allSongs.value
        } else {
            likedSongs.value
        }
    }

    LaunchedEffect(Unit) {
        fetchSongs()
        mediaPlayerViewModel.setSongs(allSongs.value)
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
                    isSelected = isAllSelected.value,
                    onClick = {
                        isAllSelected.value = true
                        isLikedSelected.value = false
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                StyledButton(
                    text = "Liked",
                    isSelected = isLikedSelected.value,
                    onClick = {
                        isLikedSelected.value = true
                        isAllSelected.value = false
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(paddingValues)
            ) {

                mediaPlayerViewModel.setSongs(getActiveSongs())
                items(getActiveSongs()) { song ->
                    Log.d("LibraryScreen", "Playing Song: ${song.title} - ${song.artist}")
                    SongCard(
                        type = "small",
                        song = song,
                        modifier = Modifier.clickable {
                            mediaPlayerViewModel.playSong(context, index = allSongs.value.indexOf(song))
                        }
                    )
                }
            }
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
