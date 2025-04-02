package com.android.purrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

data class Song(val title: String, val artist: String, val image: String)

@Composable
fun HomeScreen() {
    val newSongs = listOf(
        Song("Caramel Pain", "Hoshimachi Suisei", "#808080"),
        Song("Kirei Goto", "Hoshimachi Suisei", "#808080"),
        Song("flower rhapsody", "Sakura Miko", "#808080"),
        Song("Break It Down", "Vestia Zeta", "#808080"),
        Song("Weight of the World", "Hakos Baelz", "#808080")
    )

    val recentlyPlayed = listOf(
        Song("Haru", "Yorushika", "#808080"),
        Song("Kaisou Ressha", "Minato Aqua", "#808080"),
        Song("Hatsukoi", "Inui Toko", "#808080"),
        Song("melting", "Nakiri Ayame", "#808080"),
        Song("Catch the Moment", "LiSA", "#808080"),
    )

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
                        items(newSongs) { song ->
                            NewSongItem(song)
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
            items(recentlyPlayed) { song ->
                RecentlyPlayedItem(song)
            }
        }
    }
}

@Composable
fun NewSongItem(song: Song) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(android.graphics.Color.parseColor(song.image)))
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

@Composable
fun RecentlyPlayedItem(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(android.graphics.Color.parseColor(song.image)))
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
