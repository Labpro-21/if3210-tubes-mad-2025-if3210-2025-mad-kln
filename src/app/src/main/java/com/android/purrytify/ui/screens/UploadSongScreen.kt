package com.android.purrytify.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import android.media.MediaPlayer
import java.io.IOException
import androidx.compose.ui.platform.LocalContext


@Composable
fun UploadSongScreen(songRepository: SongRepository) {
    // State to hold the inputs
    val title = remember { mutableStateOf("") }
    val author = remember { mutableStateOf("") }
    val imagePath = remember { mutableStateOf("") }
    val songPath = remember { mutableStateOf("") }
    val isUploading = remember { mutableStateOf(false) }
    val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
    val isPlayingSong = remember { mutableStateOf(false) }


    // Image and Song pickers
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imagePath.value = it.toString() } }

    val songPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { songPath.value = it.toString() } }



    // Image Display
    @Composable
    fun DisplayImage(imageUri: String) {
        val painter = rememberImagePainter(imageUri)
        Image(
            painter = painter,
            contentDescription = "Selected Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Adjust height as necessary
            contentScale = ContentScale.Crop // Adjust scaling as necessary
        )
    }

    val context = LocalContext.current

    // Play selected song
    fun playSelectedSong(uri: Uri) {
        try {
            mediaPlayer.value?.release()
            mediaPlayer.value = MediaPlayer.create(context, uri)
            mediaPlayer.value?.start()
            isPlayingSong.value = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Stop playing song
    fun stopSong() {
        mediaPlayer.value?.stop()
        mediaPlayer.value?.release()
        mediaPlayer.value = null
        isPlayingSong.value = false
    }



    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Upload Song", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // Title input
                TextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Author input
                TextField(
                    value = author.value,
                    onValueChange = { author.value = it },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Image picker button
                Button(onClick = { imagePicker.launch("image/*") }) {
                    Text(if (imagePath.value.isEmpty()) "Select Image" else "Image Selected")
                }

                // Display selected image
                if (imagePath.value.isNotEmpty()) {
                    DisplayImage(imagePath.value)
                }


                // Show audio player if a song is selected
                Button(onClick = { songPicker.launch("audio/*") }) {
                    Text(if (songPath.value.isEmpty()) "Select Song" else "Song Selected")
                }

                // Play audio preview
                if (songPath.value.isNotEmpty()) {
                    val songUri = Uri.parse(songPath.value)

                    if (!isPlayingSong.value) {
                        Button(onClick = { playSelectedSong(songUri) }) {
                            Text("Play Song")
                        }
                    } else {
                        Button(onClick = { stopSong() }) {
                            Text("Stop Song")
                        }
                    }
                }


                // Upload button
                Button(
                    onClick = {
                        if (title.value.isNotEmpty() && author.value.isNotEmpty() &&
                            imagePath.value.isNotEmpty() && songPath.value.isNotEmpty() && !isUploading.value
                        ) {
                            val newSong = Song(
                                title = title.value,
                                author = author.value,
                                imageUri = imagePath.value,
                                songUri = songPath.value
                            )

                            // Start uploading and disable the button
                            isUploading.value = true
                            CoroutineScope(Dispatchers.IO).launch {
                                songRepository.insertSong(newSong)
                                Log.d("UploadSongScreen", "Song uploaded successfully")

                                // Clear fields after successful upload
                                title.value = ""
                                author.value = ""
                                imagePath.value = ""
                                songPath.value = ""

                                // Enable the button again
                                isUploading.value = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading.value
                ) {
                    Text(if (isUploading.value) "Uploading..." else "Upload & Save to Database")
                }
            }
        }
    }
}
