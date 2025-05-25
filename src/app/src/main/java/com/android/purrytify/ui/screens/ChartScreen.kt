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
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.android.purrytify.data.local.entities.Song

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
            userId.intValue = fetchUserId(context)!!
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

        val configuration = LocalConfiguration.current
        val scrollState = rememberScrollState()

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
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.Start
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

            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ChartInfoBox(chartViewModel, gradientColors)
                    ChartDescription(chartViewModel)
                    if (songs.isNotEmpty()) {
                        ChartSongInfoAndActions(
                            chartViewModel = chartViewModel,
                            songs = songs,
                            context = context,
                            scope = scope,
                            userId = userId,
                            isDownloading = isDownloading,
                            downloadProgress = downloadProgress,
                            mediaPlayerViewModel = mediaPlayerViewModel
                        )
                        DownloadProgressIndicator(isDownloading, downloadProgress)
                        SongList(songs, mediaPlayerViewModel, context)
                    } else {
                        NoSongsMessage()
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            } else {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChartInfoBox(chartViewModel, gradientColors, isLandscape = true)
                        ChartDescription(chartViewModel)
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (songs.isNotEmpty()) {
                            ChartSongInfoAndActions(
                                chartViewModel = chartViewModel,
                                songs = songs,
                                context = context,
                                scope = scope,
                                userId = userId,
                                isDownloading = isDownloading,
                                downloadProgress = downloadProgress,
                                mediaPlayerViewModel = mediaPlayerViewModel,
                                isLandscape = true
                            )
                            DownloadProgressIndicator(isDownloading, downloadProgress)
                            SongList(songs, mediaPlayerViewModel, context, modifier = Modifier.weight(1f))
                        } else {
                            NoSongsMessage()
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChartInfoBox(
    chartViewModel: ChartViewModel,
    gradientColors: List<Color>,
    isLandscape: Boolean = false
) {
    val boxSize = if (isLandscape) 200.dp else 250.dp
    val titleFontSize = if (isLandscape) 32.sp else 40.sp
    val subtitleFontSize = if (isLandscape) 20.sp else 24.sp

    Box(
        modifier = Modifier
            .size(width = boxSize, height = boxSize)
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
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier
                    .width(if (isLandscape) 100.dp else 140.dp)
                    .padding(vertical = if (isLandscape) 12.dp else 20.dp),
                color = Color.White.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            Text(
                text = chartViewModel.subtitle.value,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = subtitleFontSize,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ChartDescription(chartViewModel: ChartViewModel) {
    Text(
        text = chartViewModel.description.value,
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 12.sp,
        modifier = Modifier
            .padding(top = 24.dp, bottom = 12.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ChartSongInfoAndActions(
    chartViewModel: ChartViewModel,
    songs: List<Song>,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    userId: MutableState<Int>,
    isDownloading: MutableState<Boolean>,
    downloadProgress: MutableState<Float>,
    mediaPlayerViewModel: PlayerViewModel,
    isLandscape: Boolean = false
) {
    Text(
        text = chartViewModel.totalDuration.value,
        color = Color.White.copy(alpha = 0.8f),
        fontSize = if (isLandscape) 14.sp else 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 0.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            Log.d("ActionButtons", "Download button clicked")
            scope.launch {
                var downloadedCount = 0
                isDownloading.value = true
                songs.forEach { song ->
                    downloadSong(context, song, userId.value)
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
                modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)
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
                modifier = Modifier.size(if (isLandscape) 32.dp else 40.dp)
            )
        }
    }
}

@Composable
fun DownloadProgressIndicator(
    isDownloading: State<Boolean>,
    downloadProgress: State<Float>
) {
    if (downloadProgress.value < 1f && isDownloading.value) {
        DownloadProgress(downloadProgress)
    }
}

@Composable
fun SongList(
    songs: List<Song>,
    mediaPlayerViewModel: PlayerViewModel,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val lazyColumnModifier = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .heightIn(max = 240.dp)
            .fillMaxWidth()
    }

    LazyColumn(
        modifier = lazyColumnModifier,
        contentPadding = PaddingValues(bottom = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 80.dp else 16.dp),
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
}

@Composable
fun NoSongsMessage() {
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
