package com.android.purrytify.data.local.initializer

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.android.purrytify.R
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository

object SongInitializer {
    suspend fun initializeSongs(songRepository: SongRepository, context: Context) {
        withContext(Dispatchers.IO) {
            songRepository.deleteAllSongs()

            val mockSongs = createMockSongs(context)
            songRepository.insertSongs(mockSongs)
        }
    }

    private fun createMockSongs(context: Context): List<Song> {
        return listOf(
            createSong(
                id = 1,
                title = "Ghost",
                artist = "Hoshimachi Suisei",
                drawableResId = R.drawable.photo_ghost,
                audioResId = R.raw.audio_ghost,
                uploaderId = 137,
                liked = true,
                lastPlayedDate = "2025-04-11 12:00:00",
                context = context
            ),
            createSong(
                id = 2,
                title = "Shiny Smily Story",
                artist = "Hololive",
                drawableResId = R.drawable.photo_sss,
                audioResId = R.raw.audio_sss,
                uploaderId = 137,
                liked = true,
                lastPlayedDate = "2025-04-11 09:00:00",
                context = context
            ),
            createSong(
                id = 3,
                title = "Michishirube",
                artist = "Momosuzu Nene",
                drawableResId = R.drawable.photo_michishirube,
                audioResId = R.raw.audio_michishirube,
                uploaderId = 137,
                lastPlayedDate = "2025-04-11 01:00:00",
                context = context
            ),
            createSong(
                id = 4,
                title = "Kaisou Ressha",
                artist = "Minato Aqua",
                drawableResId = R.drawable.photo_kaisouressha,
                audioResId = R.raw.audio_kaisouressha,
                liked = true,
                uploaderId = 137,
                lastPlayedDate = "2025-04-11 09:00:00",
                context = context
            ),
            createSong(
                id = 5,
                title = "Inochi",
                artist = "AZKi",
                drawableResId = R.drawable.photo_inochi,
                audioResId = R.raw.audio_inochi,
                uploaderId = 138,
                lastPlayedDate = "2025-04-11 04:00:00",
                context = context
            ),
        )
    }

    private fun createSong(
        id: Int = 0,
        title: String,
        artist: String,
        drawableResId: Int,
        audioResId: Int,
        uploaderId: Int,
        liked: Boolean = false,
        lastPlayedDate: String? = null,
        context: Context
    ): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            uploaderId = uploaderId,
            imageUri = Uri.parse("android.resource://${context.packageName}/$drawableResId").toString(),
            audioUri = Uri.parse("android.resource://${context.packageName}/$audioResId").toString(),
            liked = liked,
            lastPlayedDate = lastPlayedDate
        )
    }
}
