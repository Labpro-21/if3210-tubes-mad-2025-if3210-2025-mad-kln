package com.android.purrytify.view_model

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch

class SoundCapsuleViewModel(
    private val songRepository: SongRepository = RepositoryProvider.getSongRepository(),
) : ViewModel() {

    // Songs
    var songs by mutableStateOf<List<Song>>(emptyList())
        private set

    // Time Listened
    var timeListened by mutableStateOf("0 Minutes")
        private set

    // Top Artist
    var topArtistName by mutableStateOf("")
        private set

    var topArtistImageUri by mutableStateOf("")
        private set

    var topArtistData by mutableStateOf<List<TopArtistInfo>>(emptyList())
        private set

    var artistCount by mutableIntStateOf(0)
        private set

    // Top Song
    var topSongTitle by mutableStateOf("")
        private set

    var topSongImageUri by mutableStateOf("")
        private set

    var topSongData by mutableStateOf<List<TopSongInfo>>(emptyList())
        private set

    var songCount by mutableIntStateOf(0)
        private set

    // Max Streak
    var maxStreakSongTitle by mutableStateOf("")
        private set
    var maxStreakArtistName by mutableStateOf("")
        private set
    var maxStreakCount by mutableIntStateOf(0)
        private set
    var maxStreakImageUri by mutableStateOf("")
        private set
    var maxStreakDateRange by mutableStateOf("N/A")
        private set

    fun fetchSongs(userId: Int) {
        viewModelScope.launch {
            songs = songRepository.getSongsByUploader(userId) ?: emptyList()

//            val gson = GsonBuilder().setPrettyPrinting().create()
//            songs.forEach { song ->
//                Log.d("SoundCapsuleViewModel", gson.toJson(song))
//            }

            timeListened = getTimeListened(songs)
            Log.d("SoundCapsuleViewModel", "Total Duration Played: $timeListened")

            val (artistCountVal, topArtistDataVal) = getTopArtistData(songs, 5)

            if (artistCountVal > 0 && topArtistDataVal.isNotEmpty()) {
                topArtistName = topArtistDataVal[0].artist
                topArtistImageUri = topArtistDataVal[0].imageUri
            } else {
                topArtistName = ""
                topArtistImageUri = ""
            }

            topArtistData = topArtistDataVal
            artistCount = artistCountVal

//            topArtistData.forEach{
//                artist -> Log.d("SoundCapsuleViewModel", "Top Artist: ${artist.artist}, Image URI: ${artist.imageUri}")
//            }

            val (songCountVal, songDataVal) = getTopSongData(songs, 5)

            if (songCountVal > 0 && songDataVal.isNotEmpty()) {
                topSongTitle = songDataVal[0].title
                topSongImageUri = songDataVal[0].imageUri
            } else {
                topSongTitle = ""
                topSongImageUri = ""
            }

            songCount = songCountVal
            topSongData = songDataVal

            val maxStreakInfo = getMaxStreakInfo(songs)

            maxStreakCount = maxStreakInfo.streakCount
            maxStreakSongTitle = maxStreakInfo.title
            maxStreakArtistName = maxStreakInfo.artist
            maxStreakImageUri = maxStreakInfo.imageUri
            maxStreakDateRange = maxStreakInfo.date

        }
    }
}

@Composable
fun getSoundCapsuleViewModel(): SoundCapsuleViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}

fun getTimeListened(songs: List<Song>): String {
    val totalDuration = songs.sumOf { it.secondsPlayed }
    val minutes = totalDuration / 60
    val seconds = totalDuration % 60

    return when {
        totalDuration == 0 -> "0 Minutes"
        minutes == 0 -> "$seconds Seconds"
        else -> "$minutes Minutes"
    }
}

data class TopArtistInfo(
    val rank: Int,
    val artist: String,
    val imageUri: String
)

fun getTopArtistData(songs: List<Song>, limit: Int): Pair<Int, List<TopArtistInfo>> {
    val artistListeningStats = mutableMapOf<String, Int>()

    songs.forEach { song ->
        val artist = song.artist
        val secondsPlayed = song.secondsPlayed

        artistListeningStats[artist] = artistListeningStats.getOrDefault(artist, 0) + secondsPlayed
    }

    val sortedArtists = artistListeningStats.entries.sortedByDescending { it.value }

    val topArtists = sortedArtists.take(limit).mapIndexed { index, entry ->
        val representativeSong = songs.firstOrNull { it.artist == entry.key }
        val imageUri = representativeSong?.imageUri ?: ""

        TopArtistInfo(
            rank = index + 1,
            artist = entry.key,
            imageUri = imageUri
        )
    }

    return Pair(artistListeningStats.size, topArtists)
}

data class TopSongInfo(
    val rank: Int,
    val title: String,
    val artist: String,
    val imageUri: String,
    val playCount: Int
)

fun getTopSongData(songs: List<Song>, limit: Int): Pair<Int, List<TopSongInfo>> {
    val playedSongs = songs.filter { it.secondsPlayed > 0 }
    
    val sortedSongs = playedSongs.sortedByDescending { it.secondsPlayed }

    val topSongs = sortedSongs.take(limit).mapIndexed { index, song ->
        TopSongInfo(
            rank = index + 1,
            title = song.title,
            artist = song.artist,
            imageUri = song.imageUri ?: "",
            playCount = 0
        )
    }

    return Pair(playedSongs.distinctBy { it.id }.size, topSongs)
}

data class MaxStreakInfo(
    val streakCount: Int,
    val title: String,
    val artist: String,
    val imageUri: String,
    val date: String,
)

fun getMaxStreakInfo(songs: List<Song>): MaxStreakInfo {

    val songWithTopStreak = songs.maxByOrNull { it.maxStreak }
        ?: return MaxStreakInfo(
            streakCount = 0,
            title = "No Songs",
            artist = "N/A",
            imageUri = "",
            date = "N/A"
        )

    return MaxStreakInfo(
        streakCount = songWithTopStreak.maxStreak,
        title = songWithTopStreak.title,
        artist = songWithTopStreak.artist,
        imageUri = songWithTopStreak.imageUri ?: "",
        date = songWithTopStreak.lastPlayedDate ?: "N/A"
    )
}