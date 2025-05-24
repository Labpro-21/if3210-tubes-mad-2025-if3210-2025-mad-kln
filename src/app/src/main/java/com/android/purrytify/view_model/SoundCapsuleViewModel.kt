package com.android.purrytify.view_model

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.PlaybackLog
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.PlaybackLogRepository
import com.android.purrytify.data.local.repositories.SongRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SoundCapsuleViewModel(
    private val songRepository: SongRepository = RepositoryProvider.getSongRepository(),
    private val playbackLogRepository: PlaybackLogRepository = RepositoryProvider.getPlaybackLogRepository()
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

    var songCount by mutableStateOf(0)
        private set

    fun fetchSongs(userId: Int) {
        viewModelScope.launch {
            songs = songRepository.getSongsByUploader(userId) ?: emptyList()

            val currentYearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val logs = playbackLogRepository.getLogsByYearMonth(currentYearMonth)
            
            timeListened = getTimeListened(logs)
            Log.d("SoundCapsuleViewModel", "Total Duration Played: $timeListened")

            val (artistCountVal, topArtistDataVal) = getTopArtistData(logs, songs,5)

            if (artistCountVal > 0) {
                topArtistName = topArtistDataVal[0].artist
                topArtistImageUri = topArtistDataVal[0].imageUri
            }

            topArtistData = topArtistDataVal
            artistCount = artistCountVal

            val (songCountVal, songDataVal) = getTopSongData(logs, songs,5)

            if (songCountVal > 0) {
                topSongTitle = songDataVal[0].title
                topSongImageUri = songDataVal[0].imageUri
            }

            songCount = songCountVal
            topSongData = songDataVal
        }
    }
}

@Composable
fun getSoundCapsuleViewModel(): SoundCapsuleViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}

fun getTimeListened(logs: List<PlaybackLog>): String {
    val totalDuration = logs.sumOf { it.durationMs } / 1000 // Convert to seconds
    val minutes = totalDuration / 60
    val seconds = totalDuration % 60

    return when {
        totalDuration == 0L -> "0 Minutes"
        minutes == 0L -> "$seconds Seconds"
        else -> "$minutes Minutes"
    }
}

data class TopArtistInfo(
    val rank: Int,
    val artist: String,
    val imageUri: String
)

fun getTopArtistData(logs: List<PlaybackLog>, songs: List<Song>, limit: Int): Pair<Int, List<TopArtistInfo>> {
    val artistListeningStats = mutableMapOf<String, Long>()

    logs.forEach { log ->
        val artist = log.artistName
        val durationMs = log.durationMs

        artistListeningStats[artist] = artistListeningStats.getOrDefault(artist, 0) + durationMs
    }

    val sortedArtists = artistListeningStats.entries.sortedByDescending { it.value }

    val topArtists = sortedArtists.take(limit).mapIndexed { index, entry ->
        val artistName = entry.key
        val totalTime = entry.value

        val representativeLog = logs.firstOrNull { it.artistName == artistName }
        val imageUri = representativeLog?.let { log ->
            songs.firstOrNull { it.artist == log.artistName }?.imageUri
        } ?: ""

        TopArtistInfo(
            rank = index + 1,
            artist = artistName,
            imageUri = imageUri
        )
    }

    return Pair(topArtists.size, topArtists)
}

data class TopSongInfo(
    val rank: Int,
    val title: String,
    val imageUri: String,
    val durationMs: Long
)

fun getTopSongData(logs: List<PlaybackLog>, songs: List<Song>, limit: Int): Pair<Int, List<TopSongInfo>> {
    val songListeningStats = mutableMapOf<String, Long>()

    logs.forEach { log ->
        val songId = log.songId
        val durationMs = log.durationMs

        songListeningStats[songId] = songListeningStats.getOrDefault(songId, 0) + durationMs
    }

    val sortedSongs = songListeningStats.entries.sortedByDescending { it.value }

    val topSongs = sortedSongs.take(limit).mapIndexed { index, entry ->
        val songId = entry.key
        val totalTime = entry.value

        val representativeLog = logs.firstOrNull { it.songId == songId }
        val imageUri = representativeLog?.let { log ->
            songs.firstOrNull { it.id.toString() == log.songId }?.imageUri
        } ?: ""

        TopSongInfo(
            rank = index + 1,
            title = representativeLog?.songTitle ?: "",
            imageUri = imageUri,
            durationMs = totalTime
        )
    }

    return Pair(topSongs.size, topSongs)
}