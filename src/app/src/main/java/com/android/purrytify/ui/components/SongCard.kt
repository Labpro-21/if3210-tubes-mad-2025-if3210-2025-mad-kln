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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.android.purrytify.data.local.entities.Song

@Composable
fun SongCard(type: String, song: Song?, modifier: Modifier = Modifier) {
    when (type) {
        "small" -> SmallSongCard(song, modifier)
        "large" -> LargeSongCard(song, modifier)
        else -> throw IllegalArgumentException("Invalid type: $type")
    }
}

@Composable
fun SmallSongCard(song: Song?, modifier: Modifier = Modifier) {
    val isFallback = song == null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isFallback && song!!.rank != 0) {
            Text(
                text = "${song.rank}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier
                    .width(50.dp)
                    .padding(end = 30.dp),
                textAlign = TextAlign.Start
            )
        }

        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isFallback) Color.Gray else Color.Transparent)
        ) {
            if (!isFallback) {
                Image(
                    painter = rememberAsyncImagePainter(song!!.imageUri),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = if (isFallback) " " else song!!.title,
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = if (isFallback) " " else song!!.artist,
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
fun LargeSongCard(song: Song?, modifier: Modifier = Modifier) {
    val isFallback = song == null

    Column(
        modifier = modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isFallback) Color.Gray else Color.Transparent)
        ) {
            if (!isFallback) {
                Image(
                    painter = rememberAsyncImagePainter(song!!.imageUri),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text(
            text = if (isFallback) " " else song!!.title,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = if (isFallback) " " else song!!.artist,
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}
