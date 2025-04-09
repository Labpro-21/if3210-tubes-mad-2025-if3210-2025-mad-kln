package com.android.purrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import extractDominantColor
import getCountryNameFromCode
import loadBitmapFromUrl


@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color.Black) }

    val username = "USERNAME"
    val country = getCountryNameFromCode("COUNTRY")
    val profileURL = "PROFILE URL"

    val songCount = "SONGCOUNT"
    val likeCount = "LIKECOUNT"
    val listenedCount = "LISTENEDCOUNT"

    LaunchedEffect(profileURL) {
        val bitmap = loadBitmapFromUrl(context, profileURL)
        bitmap?.let {
            dominantColor = extractDominantColor(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(dominantColor, Color.Black))
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        AsyncImage(
            model = profileURL,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(username, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(country, color = Color.LightGray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(number = songCount, label = "Songs")
            StatItem(number = likeCount, label = "Liked")
            StatItem(number = listenedCount, label = "Listened")
        }
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label.uppercase(),
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
