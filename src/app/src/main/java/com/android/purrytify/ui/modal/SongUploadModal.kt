package com.android.purrytify.ui.modal

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.purrytify.data.local.entities.Song
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import android.media.MediaPlayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import com.android.purrytify.R
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.ui.components.CustomBottomSheet
import com.android.purrytify.view_model.getPlayerViewModel
import fetchUserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.Dp


@Composable
fun SongUploadModal(
    isVisible: Boolean,
    onDismiss: (refresh: Boolean) -> Unit,
) {
    val mediaPlayerViewModel = getPlayerViewModel()
    val songRepository = RepositoryProvider.getSongRepository()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val title = remember { mutableStateOf("") }
    val artist = remember { mutableStateOf("") }
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val audioUri = remember { mutableStateOf<Uri?>(null) }

    var userId = remember { mutableStateOf(0) }
    val mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            userId.value = fetchUserId(context)!!
        }
    }

    CustomBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss
    ) {
        UploadSongContent(
            title = title,
            artist = artist,
            photoUri = photoUri,
            audioUri = audioUri,
            onCancel = {
                title.value = ""
                artist.value = ""
                photoUri.value = null
                audioUri.value = null
                mediaPlayerViewModel.clearCurrent()
                onDismiss(false)
            },
            onSave = {
                if (title.value.isNotEmpty() && artist.value.isNotEmpty() && audioUri.value != null) {
                    mediaPlayerViewModel.clearCurrent()
                    val imgRes = R.drawable.default_music_image

                    val newSong = Song(
                        title = title.value,
                        artist = artist.value,
                        imageUri = photoUri.value?.toString() ?: Uri.parse("android.resource://${context.packageName}/$imgRes").toString(),
                        audioUri = audioUri.value.toString(),
                        uploaderId = userId.value,
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        songRepository.insertSong(newSong)
                        CoroutineScope(Dispatchers.Main).launch {
                            title.value = ""
                            artist.value = ""
                            photoUri.value = null
                            audioUri.value = null
                        }
                    }
                    onDismiss(true)
                    Log.d("UploadSongModal", "Song saved: $newSong")
                }
            }
        )
    }
}

@Composable
fun UploadSongContent(
    title: MutableState<String>,
    artist: MutableState<String>,
    photoUri: MutableState<Uri?>,
    audioUri: MutableState<Uri?>,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color.Yellow)
            .padding(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF212121))
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)
        ) {
            Text(
                text = "Upload Song",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val configuration = LocalConfiguration.current
            val scrollState = rememberScrollState()

            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    UploadMediaRow(photoUri, audioUri, title, artist)
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField(title.value, { title.value = it }, "Title")
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField(artist.value, { artist.value = it }, "Artist")
                    Spacer(modifier = Modifier.height(16.dp))
                    UploadButtons(onCancel, onSave)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .verticalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(0.5f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UploadMediaRow(photoUri, audioUri, title, artist, isLandscape = true)
                    }
                    Column(
                        modifier = Modifier.weight(0.5f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            InputField(title.value, { title.value = it }, "Title")
                            Spacer(modifier = Modifier.height(16.dp))
                            InputField(artist.value, { artist.value = it }, "Artist")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        UploadButtons(onCancel, onSave, orientation = "landscape")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun UploadMediaRow(
    photoUri: MutableState<Uri?>,
    audioUri: MutableState<Uri?>,
    title: MutableState<String>,
    artist: MutableState<String>,
    isLandscape: Boolean = false
) {
    if (isLandscape) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PhotoUploadBox(uri = photoUri.value, onUpload = { photoUri.value = it }, modifier = Modifier.fillMaxWidth(0.8f))
            AudioUploadBox(uri = audioUri.value, onUpload = { audioUri.value = it }, title, artist, modifier = Modifier.fillMaxWidth(0.8f))
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PhotoUploadBox(uri = photoUri.value, onUpload = { photoUri.value = it })
            AudioUploadBox(uri = audioUri.value, onUpload = { audioUri.value = it }, title, artist)
        }
    }
}

@Composable
fun UploadButtons(onCancel: () -> Unit, onSave: () -> Unit, orientation: String = "portrait") {
    if (orientation == "portrait") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StyledButton(text = "Cancel", backgroundColor = Color(0xFF535353), onClick = onCancel)
            StyledButton(text = "Save", backgroundColor = Color(0xFF1DB955), onClick = onSave)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StyledButton(text = "Cancel", backgroundColor = Color(0xFF535353), onClick = onCancel, width = 110.dp)
            StyledButton(text = "Save", backgroundColor = Color(0xFF1DB955), onClick = onSave, width = 110.dp)
        }
    }
}

@Composable
fun StyledButton(text: String, backgroundColor: Color, onClick: () -> Unit, width: Dp = 170.dp) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(16.dp)
            .height(50.dp)
            .width(width),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = Color.White)
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
fun PhotoUploadBox(
    uri: Uri?,
    onUpload: (Uri) -> Unit,
    modifier: Modifier = Modifier.size(120.dp)
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { selectedUri ->
        selectedUri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.w("PhotoUploadBox", "Unable to persist permission for URI: $it")
            }
            onUpload(it)
        }
    }

    Box(
        modifier = modifier
            .clickable { launcher.launch(arrayOf("image/*")) }
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .aspectRatio(1f)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color.DarkGray,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    ),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.ic_song_photo),
                contentDescription = "photo_icon",
                modifier = Modifier.size(60.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Upload Photo",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ic_song_photo),
                    contentDescription = "photo_icon",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}


@Composable
fun AudioUploadBox(
    uri: Uri?,
    onUpload: (Uri) -> Unit,
    title: MutableState<String>,
    artist: MutableState<String>,
    modifier: Modifier = Modifier.size(120.dp)
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayerViewModel = getPlayerViewModel()

    val playPauseLogic: () -> Unit = {
        if (!isPlaying) {
            mediaPlayerViewModel.playSong(context,0)
        } else {
            mediaPlayerViewModel.clearCurrent()
        }
        isPlaying = !isPlaying
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { selectedUri ->
        selectedUri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.w("AudioUploadBox", "Unable to persist permission for URI: $it")
            }

            onUpload(it)

            val tempSongList = listOf(
                Song(
                    title = "temp",
                    artist = "temp",
                    imageUri = "",
                    audioUri = it.toString(),
                    uploaderId = 0
                )
            )
            mediaPlayerViewModel.setSongs(tempSongList)
            artist.value = getArtistFromUri(context, it)
            title.value = getTitleFromUri(context, it)
        }
    }


    Box(
        modifier = modifier
            .clickable { launcher.launch(arrayOf("audio/*")) }
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .aspectRatio(1f)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color.DarkGray,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    ),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top=8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPlaying) "Pause" else "Play",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    IconButton(onClick = playPauseLogic) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                    Text(
                        text = getSongDurationFormatted(context, uri),
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
                    )
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Upload File",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ic_song_audio),
                    contentDescription = "audio_icon",
                    modifier = Modifier.size(80.dp)
                )
            }
        }
    }
}

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    Column (modifier = Modifier.padding(horizontal = 16.dp))  {
        Text(
            text = label,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF212121),
                unfocusedContainerColor = Color(0xFF212121),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
        )
    }
}

fun formatTimeUpload(timeMs : Int): String {
    val minutes = (timeMs / 1000) / 60
    val seconds = (timeMs / 1000) % 60
    return "%d:%02d".format(minutes, seconds)
}


fun getSongDurationFormatted(context: Context, uri: Uri): String {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        val minutes = (durationMs / 1000) / 60
        val seconds = (durationMs / 1000) % 60
        "%d:%02d".format(minutes, seconds)
    } catch (e: Exception) {
        e.printStackTrace()
        "0:00"
    } finally {
        retriever.release()
    }
}

fun getTitleFromUri(context: Context, uri: Uri): String {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        title ?: "Unknown Title"
    } catch (e: Exception) {
        e.printStackTrace()
        "Unknown Title"
    } finally {
        retriever.release()
    }
}

fun getArtistFromUri(context: Context, uri: Uri): String {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        artist ?: "Unknown Artist"
    } catch (e: Exception) {
        e.printStackTrace()
        "Unknown Artist"
    } finally {
        retriever.release()
    }
}


