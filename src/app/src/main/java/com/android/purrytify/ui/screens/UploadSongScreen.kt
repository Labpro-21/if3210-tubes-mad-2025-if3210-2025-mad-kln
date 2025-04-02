package com.android.purrytify.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.purrytify.data.local.repositories.SongRepository
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun UploadSongScreen(songRepository: SongRepository) {
    val title = remember { mutableStateOf("") }
    val artist = remember { mutableStateOf("") }
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val audioUri = remember { mutableStateOf<Uri?>(null) }

    Scaffold(
        containerColor = Color(0xFF121212),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                Text(
                    text = "TODO:",
                    color = Color.White,
                )
                Text(
                    text = "1. Ubah title dan artist otomatis dari metadata file",
                    color = Color.White,
                )
                Text(
                    text = "2. Buat photo default jika tidak ada photo",
                    color = Color.White,
                )
                Text(
                    text = "3. Buat komponen jadi sebuah pop up",
                    color = Color.White,
                )
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
                    },
                    onSave = {
                        if (title.value.isNotEmpty() && artist.value.isNotEmpty() && photoUri.value != null && audioUri.value != null) {
                            val newSong = Song(
                                title = title.value,
                                artist = artist.value,
                                imageUri = photoUri.value.toString(),
                                songUri = audioUri.value.toString()
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

                            Log.d("UploadSongScreen", "Song saved: $newSong")
                        }
                    }
                )
            }
        }
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
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xFF212121), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            UploadHeader()
            Spacer(modifier = Modifier.height(16.dp))
            UploadMediaRow(photoUri, audioUri)
            Spacer(modifier = Modifier.height(16.dp))
            InputField(title.value, { title.value = it }, "Title")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(artist.value, { artist.value = it }, "Artist")
            UploadButtons(onCancel, onSave)
        }
    }
}

@Composable
fun UploadHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(top = 2.dp).fillMaxWidth()) {
            Box(
                modifier = Modifier.width(60.dp).height(6.dp)
                    .clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)
                    .align(Alignment.Center)
            )
        }
        Text(
            text = "Upload Song", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
        )
    }
}

@Composable
fun UploadMediaRow(photoUri: MutableState<Uri?>, audioUri: MutableState<Uri?>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PhotoUploadBox(uri = photoUri.value, onUpload = { photoUri.value = it })
        AudioUploadBox(uri = audioUri.value, onUpload = { audioUri.value = it })
    }
}

@Composable
fun UploadButtons(onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StyledButton(text = "Cancel", backgroundColor = Color(0xFF535353), onClick = onCancel)
        StyledButton(text = "Save", backgroundColor = Color(0xFF1DB955), onClick = onSave)
    }
}

@Composable
fun StyledButton(text: String, backgroundColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(16.dp).height(50.dp).width(170.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor, contentColor = Color.White)
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}

@Composable
fun PhotoUploadBox(
    uri: Uri?,
    onUpload: (Uri) -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedUri ->
        selectedUri?.let { onUpload(it) }
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clickable { launcher.launch("image/*") }
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .aspectRatio(1f)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color.DarkGray,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)),
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
                Spacer(modifier = Modifier.height(0.dp))
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
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    LaunchedEffect(uri) {
        mediaPlayer?.release()

        uri?.let {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, it)
                prepareAsync()
            }
        }
    }

    val playPauseLogic: () -> Unit = {
        if (isPlaying) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
        isPlaying = !isPlaying
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { selectedUri ->
        selectedUri?.let { onUpload(it) }
    }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clickable { launcher.launch("audio/*") }
            .background(Color(0xFF212121), RoundedCornerShape(8.dp))
            .aspectRatio(1f)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = Color.DarkGray,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPlaying) "Pause" else "Play",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    IconButton(onClick = playPauseLogic) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Upload File",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Image(
                    painter = painterResource(R.drawable.ic_song_audio),
                    contentDescription = "audio_icon",
                    modifier = Modifier.size(60.dp)
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
                unfocusedTextColor = Color.DarkGray,
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


