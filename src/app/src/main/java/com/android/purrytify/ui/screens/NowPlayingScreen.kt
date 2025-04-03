package com.android.purrytify.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import com.android.purrytify.R
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.ui.components.SongDetailButton
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(onClose: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var dominantColor by remember { mutableStateOf(Color.Black) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentTime by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(0) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val songList: List<Int> = remember {
        val rawClass = R.raw::class.java
        rawClass.fields
            .mapNotNull { field -> field.getInt(null) }
            .sortedBy { context.resources.getResourceEntryName(it) }
    }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    lateinit var nextSong: () -> Unit

    fun initializeMediaPlayer(index: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, songList[index]).apply {
            setOnPreparedListener {
                totalDuration = duration
            }
            setOnCompletionListener {
                nextSong()
            }
//            start()
        }
        currentIndex = index
//        isPlaying = true
    }

    nextSong = {
        val newIndex = (currentIndex + 1) % songList.size
        initializeMediaPlayer(newIndex)
    }

    LaunchedEffect(Unit) {
        initializeMediaPlayer(currentIndex)
    }

    val playPause: () -> Unit = {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.start()
            }
            isPlaying = it.isPlaying
        }
    }

    val previousSong: () -> Unit = {
        val newIndex = if (currentIndex - 1 < 0) songList.size - 1 else currentIndex - 1
        initializeMediaPlayer(newIndex)
    }

    LaunchedEffect(mediaPlayer) {
        while (true) {
            delay(500)
            mediaPlayer?.let {
                currentTime = it.currentPosition
                progress = it.currentPosition.toFloat() / it.duration
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    val songFileName = context.resources.getResourceEntryName(songList[currentIndex])
    val songName = songFileName.removePrefix("audio_")
    val photoResName = "photo_$songName"

    val drawableClass = R.drawable::class.java
    val photoResId = try {
        drawableClass.getField(photoResName).getInt(null)
    } catch (e: Exception) {
        0
    }

    LaunchedEffect(photoResId) {
        if (photoResId != 0) {
            val bitmap = loadBitmapFromDrawable(context, photoResId)
            dominantColor = extractDominantColor(bitmap)
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(dominantColor, Color.Black)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) { // Minimize Button
                Icon(
                    painter = painterResource(id = R.drawable.ic_caret),
                    contentDescription = "Minimize",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            SongDetailButton(song = Song(id=0, songName, "Artist Name", "", ""))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Album Art
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (photoResId != 0) {
                Image(
                    painter = painterResource(id = photoResId),
                    contentDescription = "Album Art",
                    modifier = Modifier
                        .size(336.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Song Name, Artist Name, Like Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = songName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Artist Name",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            IconButton(onClick = { /* TODO: Implementation */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = "Like",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seek Bar
        Slider(
            value = progress,
            onValueChange = { newProgress ->
                mediaPlayer?.let {
                    val newTime = (newProgress * it.duration).toInt()
                    it.seekTo(newTime)
                    currentTime = newTime
                    progress = newProgress
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.Gray
            )
        )

        // Duration Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentTime), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(formatTime(totalDuration), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = previousSong, modifier = Modifier.size(64.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_previous),
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(
                onClick = { playPause() },
                enabled = mediaPlayer != null,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )
            }

            IconButton(onClick = nextSong, modifier = Modifier.size(64.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_next),
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

fun formatTime(timeMs: Int): String {
    val minutes = (timeMs / 1000) / 60
    val seconds = (timeMs / 1000) % 60
    return "%d:%02d".format(minutes, seconds)
}

fun loadBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
    val drawable = context.resources.getDrawable(drawableId, null)
    return (drawable as BitmapDrawable).bitmap
}

fun extractDominantColor(bitmap: Bitmap): Color {
    val palette = Palette.from(bitmap).generate()
    val dominantSwatch = palette.dominantSwatch
    return if (dominantSwatch != null) Color(dominantSwatch.rgb) else Color.Black
}