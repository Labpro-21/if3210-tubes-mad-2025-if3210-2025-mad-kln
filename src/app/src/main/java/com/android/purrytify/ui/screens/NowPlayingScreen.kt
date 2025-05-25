package com.android.purrytify.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.android.purrytify.service.MusicService
import com.android.purrytify.ui.components.LikeButton
import com.android.purrytify.ui.components.ShareLinkButton
import com.android.purrytify.ui.components.ShareQRButton
import com.android.purrytify.ui.components.SongDetailButton
import com.android.purrytify.view_model.PlayerViewModel
import darkenColor
import extractDominantColor
import loadBitmapFromUri
import loadBitmapFromUrl
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

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
    val outputs by viewModel.availableOutputs.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    val isLastSong = viewModel.isLast()
    val isShuffled by viewModel.isShuffled.collectAsState()

    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color.Black) }

    LaunchedEffect(song) {
        song?.let {
            if (it.imageUri.startsWith("http")) {
                try {
                    val bitmap = loadBitmapFromUrl(context, it.imageUri)
                    bitmap?.let { bmp ->
                        dominantColor = extractDominantColor(bmp)
                    }
                } catch (e: Exception) {
                    Log.e("NowPlayingScreen", "Error loading remote image: ${e.message}")
                }
            } else {
                val bitmap = loadBitmapFromUri(context, it.imageUri)
                bitmap?.let { bmp ->
                    dominantColor = extractDominantColor(bmp)
                }
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

        val configuration = LocalConfiguration.current
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
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
                                MusicService.instance?.clearNotification()
                                onClose()
                            },
                            onEditSuccess = { updatedSong ->
                                viewModel.updateCurrentSongInfo(
                                    title = updatedSong.title,
                                    artist = updatedSong.artist,
                                    imageUri = updatedSong.imageUri
                                )
                                viewModel.updateSongInList(updatedSong)
                                MusicService.instance?.updateSongInfo(
                                    updatedSong.title,
                                    updatedSong.artist,
                                    updatedSong.imageUri
                                )
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

                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_audio_output),
                                contentDescription = "Audio Output",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            outputs.forEach { device ->
                                DropdownMenuItem(
                                    onClick = {
                                        viewModel.setAudioOutput(device)
                                        expanded = false
                                    },
                                    text = { Text(device.productName?.toString() ?: device.type.toString()) })
                            }
                        }

                        ShareLinkButton(songId = it.id)

                        ShareQRButton(songId = it.id, song = it, dominantColor = dominantColor)

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
                        onClick = { viewModel.togglePlayPause(context) },
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
        } else { // Landscape
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar: Close & Song Detail Button (remains full width)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp), // Adjusted padding
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                                MusicService.instance?.clearNotification()
                                onClose()
                            },
                            onEditSuccess = { updatedSong ->
                                viewModel.updateCurrentSongInfo(
                                    title = updatedSong.title,
                                    artist = updatedSong.artist,
                                    imageUri = updatedSong.imageUri
                                )
                                viewModel.updateSongInList(updatedSong)
                                MusicService.instance?.updateSongInfo(
                                    updatedSong.title,
                                    updatedSong.artist,
                                    updatedSong.imageUri
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp), // Outer padding for the two main columns
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Album Art
                    Column(
                        modifier = Modifier
                            .weight(0.4f) // Takes 40% of the width
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        song?.imageUri?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f) // Maintain aspect ratio
                                    .fillMaxSize(0.8f) // Occupy 80% of this column's space
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Space between album art and controls

                    // Right Column: Song Info, Progress, Controls
                    Column(
                        modifier = Modifier
                            .weight(0.6f) // Takes 60% of the width
                            .fillMaxHeight()
                            .padding(start = 8.dp), // Padding for this column's content
                        verticalArrangement = Arrangement.SpaceAround // Distribute space evenly
                    ) {
                        // Title, Artist & Action Buttons
                        song?.let {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = it.title,
                                        color = Color.White,
                                        fontSize = 20.sp, // Slightly smaller for landscape
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = it.artist,
                                        color = Color.LightGray,
                                        fontSize = 14.sp, // Slightly smaller for landscape
                                        maxLines = 1
                                    )
                                }
                                // Row for action icons to prevent them from taking too much vertical space
                                Row {
                                    IconButton(
                                        onClick = { expanded = true },
                                        modifier = Modifier.size(36.dp) // Adjusted size
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_audio_output),
                                            contentDescription = "Audio Output",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(28.dp) // Adjusted size
                                        )
                                    }
                                     DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        outputs.forEach { device ->
                                            DropdownMenuItem(
                                                onClick = {
                                                    viewModel.setAudioOutput(device)
                                                    expanded = false
                                                },
                                                text = {Text(device.productName?.toString() ?: device.type.toString())})
                                        }
                                    }

                                    ShareLinkButton(songId = it.id)
                                    ShareQRButton(songId = it.id, song = it, dominantColor = dominantColor)
                                    LikeButton(type = "heart", songId = it.id)
                                }
                            }
                        }

                        // Progress Bar & Duration (grouped together)
                        Column(modifier = Modifier.fillMaxWidth()) {
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp), // Reduced padding
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    formatTime(currentTime),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp // Slightly smaller
                                )
                                Text(
                                    formatTime(totalDuration),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp // Slightly smaller
                                )
                            }
                        }


                        // Playback Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly, // Evenly distribute for landscape
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleRepeatMode() },
                                modifier = Modifier.size(36.dp) // Adjusted size
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
                                    modifier = Modifier.size(28.dp) // Adjusted size
                                )
                            }

                            IconButton(
                                onClick = { viewModel.playPrevious(context) },
                                modifier = Modifier.size(36.dp) // Adjusted size
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_previous),
                                    contentDescription = "Previous",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp) // Adjusted size
                                )
                            }

                            IconButton(
                                onClick = { viewModel.togglePlayPause(context) },
                                modifier = Modifier.size(64.dp) // Adjusted size
                            ) {
                                Icon(
                                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp) // Adjusted size
                                )
                            }

                            IconButton(
                                onClick = { viewModel.playNext(context) },
                                enabled = !(isLastSong && repeatMode == RepeatMode.NONE),
                                modifier = Modifier.size(36.dp) // Adjusted size
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_next),
                                    contentDescription = "Next",
                                    tint = if (isLastSong && repeatMode == RepeatMode.NONE) Color.Gray else Color.White,
                                    modifier = Modifier.size(28.dp) // Adjusted size
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleShuffle() },
                                modifier = Modifier.size(36.dp) // Adjusted size
                            ) {
                                Icon(
                                    painter = painterResource(id = if (!isShuffled) R.drawable.ic_shuffle_inactive else R.drawable.ic_shuffle_active),
                                    contentDescription = "Shuffle",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(28.dp) // Adjusted size
                                )
                            }
                        }
                    }
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

@Composable
fun ShareLinkButton(songId: Int) {
    val context = LocalContext.current
    val shareLink = "https://purrytify-be.vercel.app/play?songId=$songId"

    IconButton(
        onClick = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareLink)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, "Share song")
            context.startActivity(shareIntent)
        },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_share),
            contentDescription = "Share",
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )
    }
}
