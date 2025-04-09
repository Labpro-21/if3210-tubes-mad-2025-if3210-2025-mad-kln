package com.android.purrytify.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.User
import com.android.purrytify.datastore.TokenManager
import extractDominantColor
import getCountryNameFromCode
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import loadBitmapFromUrl


@Composable
fun ProfileScreen() {
    val userRepository = RepositoryProvider.getUserRepository()
    val songRepository = RepositoryProvider.getSongRepository()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var dominantColor by remember { mutableStateOf(Color.Black) }
//    var user by remember { mutableStateOf(User(0,"","","","")) }


    var username by remember { mutableStateOf("USER") }
    var country by remember { mutableStateOf("COUNTRY") }
    var profileURL by remember { mutableStateOf("PROFILE") }

    var songCount by remember { mutableIntStateOf(0) }
    var likeCount by remember { mutableIntStateOf(0) }
    var listenedCount by remember { mutableIntStateOf(0) }

    suspend fun fetchUserId(context: Context): Int? {
        return TokenManager.getCurrentId(context).firstOrNull()
    }

    fun fetchUser() {
        coroutineScope.launch {
            val id = fetchUserId(context)

            val user = userRepository.getUserById(id!!)
            username = user!!.username
            country = getCountryNameFromCode(user.location)
            profileURL = "http://34.101.226.132:3000/uploads/profile-picture/${user.profilePhoto}"

            Log.d("DEBUG_PROFILE", "$user")

            songCount = songRepository.getSongsByUploader(id).size
            likeCount = songRepository.getLikedSongsByUploader(id).size
            listenedCount = songRepository.getListenedSongsByUploader(id).size
        }
    }



    LaunchedEffect(profileURL) {
        fetchUser()
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
            StatItem(number = songCount.toString(), label = "Songs")
            StatItem(number = likeCount.toString(), label = "Liked")
            StatItem(number = listenedCount.toString(), label = "Listened")
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
