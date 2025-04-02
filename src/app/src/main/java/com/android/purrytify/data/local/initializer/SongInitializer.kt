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
            // Clear existing data
            songRepository.deleteAllSongs()

            // Insert mock songs
            val mockSongs = createMockSongs(context)
            songRepository.insertSongs(mockSongs)
        }
    }

    private fun createMockSongs(context: Context): List<Song> {
        return listOf(
            createSong(
                title = "Ghost",
                artist = "Hoshimachi Suisei",
                drawableResId = R.drawable.photo_ghost,
                audioResId = R.raw.audio_ghost,
                context = context
            ),
            createSong(
                title = "Shiny Smily Story",
                artist = "Hololive",
                drawableResId = R.drawable.photo_sss,
                audioResId = R.raw.audio_sss,
                context = context
            ),
            createSong(
                title = "Michishirube",
                artist = "Momosuzu Nene",
                drawableResId = R.drawable.photo_michishirube,
                audioResId = R.raw.audio_michishirube,
                context = context
            ),
            createSong(
                title = "Kaisou Ressha",
                artist = "Minato Aqua",
                drawableResId = R.drawable.photo_kaisouressha,
                audioResId = R.raw.audio_kaisouressha,
                context = context
            ),
            createSong(
                title = "Inochi",
                artist = "AZKi",
                drawableResId = R.drawable.photo_inochi,
                audioResId = R.raw.audio_inochi,
                context = context
            )
        )
    }

    private fun createSong(
        title: String,
        artist: String,
        drawableResId: Int,
        audioResId: Int,
        context: Context
    ): Song {
        return Song(
            title = title,
            artist = artist,
            imageUri = Uri.parse("android.resource://${context.packageName}/$drawableResId").toString(),
            audioUri = Uri.parse("android.resource://${context.packageName}/$audioResId").toString()
        )
    }
}
