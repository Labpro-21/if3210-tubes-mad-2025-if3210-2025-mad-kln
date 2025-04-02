package com.android.purrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.android.purrytify.ui.components.SongCard
import com.android.purrytify.ui.components.SongCardProps

@Composable
fun LibraryScreen() {
    var isAllSelected = remember { mutableStateOf<Boolean>(true) }
    var isLikedSelected = remember { mutableStateOf<Boolean>(false) }

    val likedSongs = listOf(
        SongCardProps("Caramel Pain", "Hoshimachi Suisei", "#808080"),
        SongCardProps("Kirei Goto", "Hoshimachi Suisei", "#808080"),
        SongCardProps("flower rhapsody", "Sakura Miko", "#808080"),
        SongCardProps("Break It Down", "Vestia Zeta", "#808080"),
        SongCardProps("Weight of the World", "Hakos Baelz", "#808080")
    )

    val allSongs = listOf(
        SongCardProps("Haru", "Yorushika", "#808080"),
        SongCardProps("Kaisou Ressha", "Minato Aqua", "#808080"),
        SongCardProps("Hatsukoi", "Inui Toko", "#808080"),
        SongCardProps("melting", "Nakiri Ayame", "#808080"),
        SongCardProps("Catch the Moment", "LiSA", "#808080"),
        SongCardProps("Caramel Pain", "Hoshimachi Suisei", "#808080"),
        SongCardProps("Kirei Goto", "Hoshimachi Suisei", "#808080"),
        SongCardProps("flower rhapsody", "Sakura Miko", "#808080"),
        SongCardProps("Break It Down", "Vestia Zeta", "#808080"),
        SongCardProps("Weight of the World", "Hakos Baelz", "#808080"),
        SongCardProps("Haru", "Yorushika", "#808080"),
        SongCardProps("Kaisou Ressha", "Minato Aqua", "#808080"),
        SongCardProps("Hatsukoi", "Inui Toko", "#808080"),
        SongCardProps("melting", "Nakiri Ayame", "#808080"),
        SongCardProps("Catch the Moment", "LiSA", "#808080"),
        SongCardProps("Caramel Pain", "Hoshimachi Suisei", "#808080"),
        SongCardProps("Kirei Goto", "Hoshimachi Suisei", "#808080"),
        SongCardProps("flower rhapsody", "Sakura Miko", "#808080"),
        SongCardProps("Break It Down", "Vestia Zeta", "#808080"),
        SongCardProps("Weight of the World", "Hakos Baelz", "#808080")
    )

    Scaffold(
        containerColor = Color(0xFF121212),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            PageHeader()

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
                items(if (isAllSelected.value) allSongs else likedSongs) { song ->
                    SongCard(type = "small", song = song)
                }
            }
        }
    }
}

@Composable
fun PageHeader() {
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
        Button(
            onClick = {  },
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

