package com.android.purrytify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.android.purrytify.R
import com.android.purrytify.data.local.repositories.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LikeButton(
    songId: Int,
    songRepository: SongRepository
) {
    val isLiked = remember { mutableStateOf(false) }

    LaunchedEffect(songId) {
        val liked = songRepository.isSongLiked(songId)
        isLiked.value = liked ?: false
    }

    IconButton(onClick = {
        CoroutineScope(Dispatchers.IO).launch {
            songRepository.toggleLikeSong(songId, !isLiked.value)
            val updatedSong = songRepository.getSongById(songId)
            withContext(Dispatchers.Main) {
                isLiked.value = updatedSong?.liked ?: false
            }
        }
    }) {
        val iconRes = if (isLiked.value) R.drawable.ic_favourite_active else R.drawable.ic_favourite_inactive
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = if (isLiked.value) "Liked" else "Not Liked"
        )
    }
}
