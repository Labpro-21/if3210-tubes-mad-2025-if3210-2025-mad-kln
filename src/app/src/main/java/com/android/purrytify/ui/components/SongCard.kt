package com.android.purrytify.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale

data class SongCardProps(
    val title: String,
    val artist: String,
    val imageUri: Uri,
)

@Composable
fun SongCard(type: String, song: SongCardProps) {
    when (type) {
        "small" -> SmallSongCard(song)
        "large" -> LargeSongCard(song)
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
}

@Composable
fun SmallSongCard(song: SongCardProps) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(song.imageUri),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = song.artist,
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LargeSongCard(song: SongCardProps) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(song.imageUri),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = song.artist,
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}