package com.android.purrytify.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.entities.User
import com.android.purrytify.datastore.TokenManager
import com.android.purrytify.network.RetrofitClient
import com.android.purrytify.network.checkToken
import com.android.purrytify.view_model.PlayerViewModel
import com.android.purrytify.view_model.getPlayerViewModel
import extractDominantColor
import getCountryNameFromCode
import getToken
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import loadBitmapFromUrl


@Composable
fun ProfileScreen(
    navController: NavController,
    mediaPlayerViewModel: PlayerViewModel = getPlayerViewModel()
) {
    val userRepository = RepositoryProvider.getUserRepository()
    val songRepository = RepositoryProvider.getSongRepository()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var dominantColor by remember { mutableStateOf(Color.Black) }

    var username by remember { mutableStateOf("USER") }
    var country by remember { mutableStateOf("COUNTRY") }
    var profileURL by remember { mutableStateOf("") }

    var songCount by remember { mutableIntStateOf(0) }
    var likeCount by remember { mutableIntStateOf(0) }
    var listenedCount by remember { mutableIntStateOf(0) }
    
    // Add new states for listening statistics
    var totalListeningTime by remember { mutableStateOf("0 s") }
    var maxStreak by remember { mutableIntStateOf(0) }
    var topArtists by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var mostListenedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var LongestCurrentStreakSongs by remember { mutableStateOf<List<Song>>(emptyList()) }

    suspend fun fetchUserId(context: Context): Int {
        Log.d("DEBUG_PROFILE", "Fetching user ID")
        return TokenManager.getCurrentId(context).firstOrNull()!!
    }

    fun fetchUser() {
        coroutineScope.launch {
            checkToken(context)
            val id = fetchUserId(context)
            Log.d("DEBUG_PROFILE", "Fetched user ID: $id")

            val bearerToken = "Bearer ${getToken(context)}"
            val user = RetrofitClient.api.getProfile(bearerToken)

            Log.d("DEBUG_PROFILE", "Fetched user: $user")
            username = user.username
            country = getCountryNameFromCode(user.location)
            profileURL = "http://34.101.226.132:3000/uploads/profile-picture/${user.profilePhoto}"

            Log.d("DEBUG_PROFILE", "profile url: $profileURL")

            val songs = songRepository.getSongsByUploader(id)
            songCount = songs.size
            likeCount = songRepository.getLikedSongsByUploader(id).size
            listenedCount = songRepository.getListenedSongsCount(id)

            Log.d("DEBUG_PROFILE", "song count: $songCount")
            Log.d("DEBUG_PROFILE", "like count: $likeCount")
            Log.d("DEBUG_PROFILE", "listened count: $listenedCount")
            
            // Fetch listening statistics
            val totalSeconds = songRepository.getTotalListeningTime(id)
            totalListeningTime = songRepository.formatListeningTime(totalSeconds)
            
            maxStreak = songRepository.getMaxStreak(id)
            topArtists = songRepository.getTopArtistsByListeningTime(id, 5)
            mostListenedSongs = songRepository.getMostListenedSongs(id, 5)
            LongestCurrentStreakSongs = songRepository.getDayStreak(id, 5)
            
            Log.d("DEBUG_PROFILE", "Total listening time: $totalListeningTime")
            Log.d("DEBUG_PROFILE", "Max streak: $maxStreak days")
        }
    }

    fun logout() {
        coroutineScope.launch {
            TokenManager.clearToken(context)
            Log.d("DEBUG_PROFILE", "Logged out")
            mediaPlayerViewModel.clearCurrent()
            navController.navigate("login")
        }
    }

    LaunchedEffect(Unit) {
        fetchUser()
    }
    LaunchedEffect(profileURL) {
        if (profileURL.isNotBlank()) {
            val bitmap = loadBitmapFromUrl(context, profileURL)
            bitmap?.let {
                dominantColor = extractDominantColor(it)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(dominantColor, Color(0xFF121212), Color(0xFF121212)))
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
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

            Button(
                onClick = { logout() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(number = songCount.toString(), label = "Songs")
                StatItem(number = likeCount.toString(), label = "Liked")
                StatItem(number = listenedCount.toString(), label = "Listened")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // New listening statistics section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF212121)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Listening Statistics",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(number = totalListeningTime, label = "Total Time")
                        StatItem(number = "$maxStreak days", label = "Max Streak")
                    }
                    
                    if (topArtists.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Top Artists",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        topArtists.entries.take(3).forEach { (artist, seconds) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    artist,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    songRepository.formatListeningTime(seconds),
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    if (mostListenedSongs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Most Listened Songs",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        mostListenedSongs.take(3).forEach { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        song.title,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        song.artist,
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    songRepository.formatListeningTime(song.secondsPlayed),
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (LongestCurrentStreakSongs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Most Listened Songs",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LongestCurrentStreakSongs.take(3).forEach { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        song.title,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        song.artist,
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    song.dayStreak.toString(),
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for player
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
