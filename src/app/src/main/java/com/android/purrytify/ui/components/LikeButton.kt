package com.android.purrytify.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.purrytify.R
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.repositories.SongRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LikeButton(
    type: String,
    songId: Int,
//    callback: () -> Unit = {}
) {
    val isLiked = remember { mutableStateOf(false) }
    val songRepository = RepositoryProvider.getSongRepository()

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

        val iconRes: Int = when (type) {
            "heart" -> if (isLiked.value) R.drawable.ic_favourite_active else R.drawable.ic_favourite_inactive
            "plus" -> if (isLiked.value) R.drawable.ic_check_circle else R.drawable.ic_like
            else -> R.drawable.ic_favourite_inactive
        }
        val iconSize = when {
            type == "plus" && isLiked.value -> 24.dp
            type == "plus" && !isLiked.value -> 18.dp
            else -> 32.dp
        }
        val iconTint = if (type == "plus" && isLiked.value) Color(0xFF1DB954) else Color.White

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = if (isLiked.value) "Liked" else "Not Liked",
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )

    }
}
