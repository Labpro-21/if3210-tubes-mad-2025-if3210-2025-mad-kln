package com.android.purrytify.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.purrytify.R
import com.android.purrytify.controller.RepeatMode
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.ui.components.LikeButton
import com.android.purrytify.ui.components.SongDetailButton
import com.android.purrytify.view_model.PlayerViewModel
import darkenColor
import extractDominantColor
import loadBitmapFromUri

@Composable
fun NowPlayingScreen(
    viewModel: PlayerViewModel,
    onClose: () -> Unit
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    val songRepository = RepositoryProvider.getSongRepository()
    val isLastSong = viewModel.isLast()
    val isShuffled by viewModel.isShuffled.collectAsState()

    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color.Black) }

    LaunchedEffect(song) {
        song?.let {
            songRepository.updateLastPlayedDate(it.id)

            val bitmap = loadBitmapFromUri(context, it.imageUri)
            bitmap?.let { bmp ->
                dominantColor = extractDominantColor(bmp)
            }

        } ?: onClose()
    }

    val darkerColor = darkenColor(dominantColor, 0.1f)
    val gradient = Brush.verticalGradient(
        colors = listOf(dominantColor, darkerColor)
    )

    val flagDim = dominantColor.luminance() > 0.5f
    val scrimColor = if (flagDim) Color.Black.copy(alpha = 0.3f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(scrimColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            // Close & Song Detail Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caret),
                        contentDescription = "Minimize",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

            song?.let {
                SongDetailButton(
                    song = it,
                    onDeleteSuccess = {
                        viewModel.clearCurrent()
                        onClose()
                    },
                    onEditSuccess = { updatedSong ->
                        viewModel.updateCurrentSongInfo(
                            title = updatedSong.title,
                            artist = updatedSong.artist,
                            imageUri = updatedSong.imageUri
                        )
                        viewModel.updateSongInList(updatedSong)
                    }
                )
            }
        }

            Spacer(modifier = Modifier.height(32.dp))

            // Album Art
            song?.imageUri?.let {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(336.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title, Artist, Like Button
            song?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = it.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it.artist,
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                    }

                    LikeButton(
                        type = "heart",
                        songId = it.id,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            Slider(
                value = progress,
                onValueChange = { viewModel.seekTo(it) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                )
            )

            // Duration
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatTime(currentTime),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    formatTime(totalDuration),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Playback
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = { viewModel.toggleRepeatMode() },
                    modifier = Modifier.size(40.dp)
                ) {
                    val repeatIcon = when (repeatMode) {
                        RepeatMode.NONE -> R.drawable.ic_repeat_none
                        RepeatMode.REPEAT_ALL -> R.drawable.ic_repeat_all
                        RepeatMode.REPEAT_ONE -> R.drawable.ic_repeat_one
                    }
                    Icon(
                        painter = painterResource(id = repeatIcon),
                        contentDescription = "Repeat Mode",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.playPrevious(context) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_previous),
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.playNext(context) },
                    enabled = !(isLastSong && repeatMode == RepeatMode.NONE),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_next),
                        contentDescription = "Next",
                        tint = if (isLastSong && repeatMode == RepeatMode.NONE) Color.Gray else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (!isShuffled) R.drawable.ic_shuffle_inactive else R.drawable.ic_shuffle_active),
                        contentDescription = "Shuffle",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}


fun formatTime(timeMs: Int): String {
    val minutes = (timeMs / 1000) / 60
    val seconds = (timeMs / 1000) % 60
    return "%d:%02d".format(minutes, seconds)
}

