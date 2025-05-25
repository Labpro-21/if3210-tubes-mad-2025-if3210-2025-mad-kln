package com.android.purrytify.ui.modal

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.purrytify.R
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.ui.components.CustomBottomSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import downloadSong
import fetchUserId

@Composable
fun SongDetailModal(
    song: Song,
    onEditClick: () -> Unit,
    onDismiss: (Boolean) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val showConfirmDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isOnlineSong = song.uploaderId == 0
    val isOfflineSong = song.uploaderId != 0
    val canDownload = isOnlineSong && !song.isDownloaded

    if (showConfirmDialog.value) {
        ConfirmDeleteDialog(
            onConfirm = {
                showConfirmDialog.value = false
                CoroutineScope(Dispatchers.IO).launch {
                    RepositoryProvider.getSongRepository().deleteSong(song.id)
                    withContext(Dispatchers.Main) {
                        onDeleteSuccess()
                        onDismiss(true)
                    }
                }
            },
            onCancel = { showConfirmDialog.value = false }
        )
    }

    CustomBottomSheet(
        isVisible = true,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isOfflineSong) {
                SongDetailActionButton(
                    iconRes = R.drawable.ic_edit,
                    text = "Edit",
                    onClick = onEditClick
                )

                Spacer(modifier = Modifier.height(4.dp))

                SongDetailActionButton(
                    iconRes = R.drawable.ic_delete,
                    text = "Delete",
                    onClick = { showConfirmDialog.value = true }
                )
            }

            if (canDownload) {
                SongDetailActionButton(
                    iconRes = R.drawable.ic_download,
                    text = "Download",
                    onClick = {
                        Log.d("Download Song","Downloading $song")
                        scope.launch {
                            try {
                                val userId = fetchUserId(context)
                                downloadSong(context, song, userId)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Song downloaded successfully", Toast.LENGTH_SHORT).show()
                                }
                                onDismiss(true)
                            } catch (e: Exception) {
                                Log.e("Download Song", "Error downloading: ${e.message}")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Failed to download song: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                onDismiss(true)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = { onCancel() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray, shape = MaterialTheme.shapes.medium)
                .padding(24.dp)
        ) {
            Text(
                text = "Delete Song?",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Are you sure you want to delete this song? This action cannot be undone.",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(onClick = onConfirm) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun SongDetailActionButton(iconRes: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}