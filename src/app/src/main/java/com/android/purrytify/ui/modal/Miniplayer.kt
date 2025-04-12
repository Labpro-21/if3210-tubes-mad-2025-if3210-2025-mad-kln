package com.android.purrytify.ui.modal

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.android.purrytify.R
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.ui.components.LikeButton
import extractDominantColor
import com.android.purrytify.view_model.PlayerViewModel
import darkenColor
import loadBitmapFromUri

@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel,
    onOpenFullPlayer: () -> Unit,
) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()

    var dominantColor by remember { mutableStateOf(Color.Black) }
    val context = LocalContext.current

    val songRepository = RepositoryProvider.getSongRepository()

    LaunchedEffect(song?.imageUri) {
        song?.imageUri?.let { uri ->
            val bitmap = loadBitmapFromUri(context, uri)
            bitmap?.let {
                dominantColor = extractDominantColor(it)
            }
        }
    }

    LaunchedEffect(song?.id) {
        song?.let {
            songRepository.updateLastPlayedDate(it.id)
        }
    }

    val backgroundColor = darkenColor(dominantColor,0.5f)

    AnimatedVisibility(visible = song != null, enter = fadeIn(), exit = fadeOut()) {
        song?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { onOpenFullPlayer() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(it.imageUri),
                        contentDescription = "Album Art",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(it.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(it.artist, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                    }

                    LikeButton(
                        type = "plus",
                        songId = it.id,
                    )

                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Gray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(2.dp)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}



