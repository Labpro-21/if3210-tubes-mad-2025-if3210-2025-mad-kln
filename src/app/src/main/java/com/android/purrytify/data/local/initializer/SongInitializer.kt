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
                imageResId = R.drawable.photo_ghost,
                audioResId = R.raw.audio_ghost,
                uploaderId = 137,
                liked = true,
                lastPlayedDate = "2025-04-11 12:00:00",
                context = context,
                duration = 240,
            ),
            createSong(
                id = 2,
                title = "Shiny Smily Story",
                artist = "Hololive",
                imageResId = R.drawable.photo_sss,
                audioResId = R.raw.audio_sss,
                uploaderId = 137,
                liked = true,
                lastPlayedDate = "2025-04-11 09:00:00",
                context = context,
                duration = 70,
            ),
            createSong(
                id = 3,
                title = "Michishirube",
                artist = "Momosuzu Nene",
                imageResId = R.drawable.photo_michishirube,
                audioResId = R.raw.audio_michishirube,
                uploaderId = 137,
                lastPlayedDate = "2025-04-11 01:00:00",
                context = context,
                duration = 60,
            ),
            createSong(
                id = 4,
                title = "Kaisou Ressha",
                artist = "Minato Aqua",
                imageResId = R.drawable.photo_kaisouressha,
                audioResId = R.raw.audio_kaisouressha,
                liked = true,
                uploaderId = 137,
                lastPlayedDate = "2025-04-11 09:00:00",
                context = context,
                duration = 140,
            ),
            createSong(
                id = 5,
                title = "Inochi",
                artist = "AZKi",
                imageResId = R.drawable.photo_inochi,
                audioResId = R.raw.audio_inochi,
                uploaderId = 137,
                lastPlayedDate = "2025-04-11 04:00:00",
                context = context,
                duration = 150,
            ),
            createSong(
                id = 6,
                title = "Counting Stars",
                artist = "Nujabes",
                imageResId = R.drawable.photo_counting_stars,
                audioResId = R.raw.audio_counting_stars,
                uploaderId = 127,
                lastPlayedDate = "2025-04-12 01:00:00",
                context = context
            ),
            createSong(
                id = 7,
                title = "Just Forget",
                artist = "FORCE OF NATURE",
                imageResId = R.drawable.photo_just_forget,
                audioResId = R.raw.audio_just_forget,
                uploaderId = 127,
                lastPlayedDate = "2025-04-12 02:30:00",
                context = context
            ),
            createSong(
                id = 8,
                title = "Shiki No Uta   ",
                artist = "MINMI",
                imageResId = R.drawable.photo_shiki_no_uta,
                audioResId = R.raw.audio_shiki_no_uta,
                uploaderId = 127,
                lastPlayedDate = "2025-04-12 04:00:00",
                context = context
            ),
            createSong(
                id = 9,
                title = "Who's Theme",
                artist = "MINMI",
                imageResId = R.drawable.photo_whos_theme,
                audioResId = R.raw.audio_whos_theme,
                uploaderId = 127,
                lastPlayedDate = "2025-04-12 05:30:00",
                context = context
            ),
            createSong(
                id = 10,
                title = "愛密集",
                artist = "Yakkle",
                imageResId = R.drawable.photo_i_miss_you,
                audioResId = R.raw.audio_i_miss_you,
                uploaderId = 127,
                lastPlayedDate = "2025-05-18 12:00:00",
                context = context
            ),
            createSong(
                id = 11,
                title = "魔法",
                artist = "Myuk",
                imageResId = R.drawable.photo_mahou,
                audioResId = R.raw.audio_mahou,
                uploaderId = 138,
                lastPlayedDate = "2025-04-12 07:00:00",
                context = context
            ),
            createSong(
                id = 12,
                title = "たふ_ん",
                artist = "Yoasobi",
                imageResId = R.drawable.photo_tabun,
                audioResId = R.raw.audio_tabun,
                uploaderId = 138,
                lastPlayedDate = "2025-04-12 07:00:00",
                context = context
            ),
            createSong(
                id = 13,
                title = "Take Me Home",
                artist = "Cash Cash",
                imageResId = R.drawable.photo_take_me_home,
                audioResId = R.raw.audio_take_me_home,
                uploaderId = 138,
                lastPlayedDate = "2025-04-12 07:00:00",
                context = context
            ),
            createSong(
                id = 14,
                title = "Never Gonna Give You Up",
                artist = "Rick Astley",
                imageResId = R.drawable.photo_rick,
                audioResId = R.raw.audio_rick,
                uploaderId = 138,
                lastPlayedDate = "2025-04-12 07:00:00",
                context = context
            ),
            createSong(
                id = 15,
                title = "Old Town Road",
                artist = "Lil Nas X",
                imageResId = R.drawable.photo_old_town_road,
                audioResId = R.raw.audio_old_town_road,
                uploaderId = 138,
                lastPlayedDate = "2025-04-12 07:00:00",
                context = context
            )
        )
    }

    private fun createSong(
        id: Int = 0,
        title: String,
        artist: String,
        imageResId: Int,
        audioResId: Int,
        duration: Int = 0,
        uploaderId: Int = 0,
        country: String = "",
        liked: Boolean = false,
        lastPlayedDate: String = "",
        context: Context
    ): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            uploaderId = uploaderId,
            imageUri = Uri.parse("android.resource://${context.packageName}/$imageResId").toString(),
            audioUri = Uri.parse("android.resource://${context.packageName}/$audioResId").toString(),
            liked = liked,
            lastPlayedDate = lastPlayedDate,
            duration = duration,
        )
    }
}
