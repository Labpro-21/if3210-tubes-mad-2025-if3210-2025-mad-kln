package com.android.purrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.android.purrytify.ui.components.SongCardFake
import com.android.purrytify.ui.components.SongCardFakeProps

@Composable
fun HomeScreen() {
    val newSongs = listOf(
        SongCardFakeProps("Caramel Pain", "Hoshimachi Suisei", "#808080"),
        SongCardFakeProps("Kirei Goto", "Hoshimachi Suisei", "#808080"),
        SongCardFakeProps("flower rhapsody", "Sakura Miko", "#808080"),
        SongCardFakeProps("Break It Down", "Vestia Zeta", "#808080"),
        SongCardFakeProps("Weight of the World", "Hakos Baelz", "#808080")
    )

    val recentlyPlayed = listOf(
        SongCardFakeProps("Haru", "Yorushika", "#808080"),
        SongCardFakeProps("Kaisou Ressha", "Minato Aqua", "#808080"),
        SongCardFakeProps("Hatsukoi", "Inui Toko", "#808080"),
        SongCardFakeProps("melting", "Nakiri Ayame", "#808080"),
        SongCardFakeProps("Catch the Moment", "LiSA", "#808080"),
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
                            SongCardFake(type = "large", song = song)
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
                SongCardFake(type = "small", song = song)
            }
        }
    }
}