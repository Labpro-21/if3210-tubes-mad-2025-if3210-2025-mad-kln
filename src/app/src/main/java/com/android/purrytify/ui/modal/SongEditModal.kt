package com.android.purrytify.ui.modal

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.ui.components.CustomBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SongEditModal(
    song: Song,
    onDismiss: (refresh: Boolean) -> Unit,
    onEditSuccess: (String, String, String) -> Unit
) {
    val songRepository = RepositoryProvider.getSongRepository()

    val title = remember { mutableStateOf(song.title) }
    val artist = remember { mutableStateOf(song.artist) }
    val photoUri = remember { mutableStateOf<Uri?>(Uri.parse(song.imageUri))}
    val audioUri = remember { mutableStateOf<Uri?>(Uri.parse(song.audioUri)) }

    CustomBottomSheet(
        isVisible = true,
        onDismiss = onDismiss
    ) {
        EditSongContent(
            title = title,
            artist = artist,
            photoUri = photoUri,
            onCancel = {
                onDismiss(false)
            },
            onSave = {
                if (title.value.isNotEmpty() && artist.value.isNotEmpty() && photoUri.value != null && audioUri.value != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        songRepository.updateSongInfo(
                            songId = song.id,
                            imageUri = photoUri.value.toString(),
                            artist = artist.value,
                            title = title.value
                        )
                    }
                    onEditSuccess(
                        title.value,
                        artist.value,
                        photoUri.value.toString()
                    )
                    onDismiss(true)
                    Log.d("SongEditModal", "Song updated: $title")
                }
            }
        )
    }
}

@Composable
fun EditSongContent(
    title: MutableState<String>,
    artist: MutableState<String>,
    photoUri: MutableState<Uri?>,
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
                text = "Edit Song",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            EditMediaRow(photoUri)
            Spacer(modifier = Modifier.height(16.dp))
            InputField(title.value, { title.value = it }, "Title")
            Spacer(modifier = Modifier.height(16.dp))
            InputField(artist.value, { artist.value = it }, "Artist")
            UploadButtons(onCancel, onSave)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditMediaRow(photoUri: MutableState<Uri?>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PhotoUploadBox(uri = photoUri.value, onUpload = { photoUri.value = it })
    }
}
