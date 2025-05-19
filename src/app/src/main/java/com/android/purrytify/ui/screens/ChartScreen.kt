package com.android.purrytify.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purrytify.R
import com.android.purrytify.ui.components.SongCard
import com.android.purrytify.view_model.ChartViewModel
import com.android.purrytify.view_model.PlayerViewModel
import com.android.purrytify.view_model.getChartViewModel
import com.android.purrytify.view_model.getPlayerViewModel
import downloadSong
import fetchUserId
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@Composable
fun DownloadProgress(downloadProgress: State<Float>) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .wrapContentSize(Alignment.Center)
            .padding(horizontal = 32.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Downloading ${String.format("%.0f", downloadProgress.value * 100)}%",
            color = Color.LightGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        LinearProgressIndicator(
            progress = { downloadProgress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color.LightGray,
            trackColor = Color.Gray.copy(alpha = 0.5f),
        )
    }
}

fun colorGradientFromString(color: String): List<Color> {
    return when (color.lowercase()) {
        "red" -> listOf(
            Color(0xFFE87A7A),
            Color(0xFFD80832)
        )
        "blue" -> listOf(
            Color(0xCB47C1AA),
            Color(0xFF0C2E6E)
        )
        "green" -> listOf(
            Color(0xFF69F0AE),
            Color(0xFF0B9843)
        )
        else -> listOf(
            Color(0xFF78909C),
            Color(0xFF455A64)
        )
    }
}

@Composable
fun ChartScreen(
    onClose: () -> Unit,
    chartViewModel: ChartViewModel = getChartViewModel(),
    mediaPlayerViewModel: PlayerViewModel = getPlayerViewModel(),
) {
    val gradientColors = colorGradientFromString(chartViewModel.color.value)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val outerGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to gradientColors[0],
            0.25f to gradientColors[1],
            0.55f to Color.Black,
        )
    )

    val flagDim = gradientColors[0].luminance() > 0.5f
    val scrimColor = if (flagDim) Color.Black.copy(alpha = 0.3f) else Color.Transparent

    val songs by chartViewModel.chartSongs

    val userId = remember { mutableIntStateOf(0) }
    val downloadProgress = remember { mutableStateOf(0f) }
    val isDownloading = remember { mutableStateOf(false) }

    fun fetchUser() {
        scope.launch {
            Log.d("LibraryScreen", "Fetching user ID")
            userId.intValue = fetchUserId(context)
            Log.d("LibraryScreen", "Fetched user ID: ${userId.intValue}")
        }
    }

    LaunchedEffect(Unit) {
        fetchUser()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerGradient)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(scrimColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_caret),
                        contentDescription = "Minimize",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(width = 250.dp, height = 250.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.verticalGradient(gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = chartViewModel.title.value,
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .width(140.dp)
                            .padding(vertical = 20.dp),
                        color = Color.White.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Text(
                        text = chartViewModel.subtitle.value,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 24.sp
                    )
                }
            }

            Text(
                text = chartViewModel.description.value,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 12.dp, start= 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (songs.isNotEmpty()) {
                Text(
                    text = chartViewModel.totalDuration.value,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 0.dp, start= 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        Log.d("ActionButtons", "Download button clicked")
                        scope.launch {
                            var downloadedCount = 0
                            isDownloading.value = true
                            songs.forEach { song ->
                                downloadSong(context, song, userId.intValue)
                                downloadedCount++
                                downloadProgress.value = downloadedCount.toFloat() / songs.size.toFloat()
                            }
                            isDownloading.value = false
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download),
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(onClick = {
                        Log.d("ActionButtons", "Play button clicked")
                        mediaPlayerViewModel.setSongs(songs)
                        mediaPlayerViewModel.playSong(context, 0)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "Play",
                            tint = Color(0xFF1DB95B),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                if (downloadProgress.value < 1f && isDownloading.value) {
                    DownloadProgress(downloadProgress)
                }

                LazyColumn(
                    modifier = Modifier
                        .height(240.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(songs) { song ->
                        SongCard(
                            type = "small",
                            song = song,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    mediaPlayerViewModel.setSongs(songs)
                                    mediaPlayerViewModel.playSong(context, songs.indexOf(song))
                                }
                        )
                    }
                }
            } else {
                Text(
                    text = "Songs are not available for this chart.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
