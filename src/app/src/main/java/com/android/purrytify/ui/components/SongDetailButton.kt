package com.android.purrytify.ui.components

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.purrytify.R
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.ui.modal.SongEditModal
import com.android.purrytify.ui.modal.SongDetailModal

@Composable
fun SongDetailButton(song: Song, onDeleteSuccess: () -> Unit) {
    val isDetailModalVisible = remember { mutableStateOf(false) }
    val isEditModalVisible = remember { mutableStateOf(false) }

    IconButton(onClick = {
        isDetailModalVisible.value = true
        Log.d("SongDetailButton", "Detail: $song")
    }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_more),
            contentDescription = "More",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }

    if (isDetailModalVisible.value) {
        SongDetailModal(
            song = song,
            onEditClick = {
                isDetailModalVisible.value = false
                isEditModalVisible.value = true
            },
            onDismiss = { isDetailModalVisible.value = false },
            onDeleteSuccess = {
                isDetailModalVisible.value = false
                onDeleteSuccess()
            }
        )
    }

    if (isEditModalVisible.value) {
        SongEditModal(
            song = song,
            onDismiss = { isEditModalVisible.value = false }
        )
    }
}



