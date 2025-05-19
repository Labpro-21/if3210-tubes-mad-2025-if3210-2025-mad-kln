package com.android.purrytify.data.local.repositories

import android.util.Log
import com.android.purrytify.data.local.dao.SongDao
import com.android.purrytify.data.local.entities.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class SongRepository(private val songDao: SongDao) {

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun insertSong(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            val finalSong = if (song.uploadDate == "") {
                song.copy(uploadDate = getCurrentDate())
            } else {
                song
            }

            songDao.insertSong(finalSong)
            Log.d("Database", "Inserted Song: ${finalSong.title} by ${finalSong.artist}")
        }
    }

    fun insertSongs(songs: List<Song>) {
        CoroutineScope(Dispatchers.IO).launch {
            for (song in songs) {
                insertSong(song)
            }
        }
    }

    fun deleteAllSongs() {
        CoroutineScope(Dispatchers.IO).launch {
            songDao.deleteAllSongs()
        }
    }

    suspend fun getSongById(songId: Int): Song? {
        return withContext(Dispatchers.IO) {
            songDao.getSongById(songId)
        }
    }

    suspend fun getAllSongs(): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getAllSongs()
        }
    }

    suspend fun deleteSong(songId: Int) {
        withContext(Dispatchers.IO) {
            songDao.deleteSong(songId)
        }
    }

    suspend fun updateLastPlayedDate(song: Song) {
        val today = LocalDate.now()
        val lastPlayed = song.lastPlayedDate
            .takeIf { it.length >= 10 }
            ?.substring(0, 10)
            ?.let { LocalDate.parse(it) }

        if (lastPlayed == today) return

        val newStreak = if (lastPlayed == today.minusDays(1)) song.dayStreak + 1 else 1
        val maxStreak = maxOf(song.maxStreak, newStreak)

        songDao.updateLastPlayedDate(song.id, today.toString(), newStreak, maxStreak)
    }


    suspend fun getSongsByUploader(uploaderId: Int, query: String = ""): List<Song> {

        if (query != "") {
            return withContext(Dispatchers.IO) {
                songDao.searchSongsByUploader(uploaderId, query)
            }
        }
        return withContext(Dispatchers.IO) {
            songDao.getSongsByUploader(uploaderId)
        }
    }

    suspend fun getLikedSongsByUploader(uploaderId: Int, query: String = ""): List<Song> {
        if (query != "") {
            return withContext(Dispatchers.IO) {
                songDao.searchLikedSongsByUploader(uploaderId, query)
            }
        }
        return withContext(Dispatchers.IO) {
            songDao.getLikedSongsByUploader(uploaderId)
        }
    }

    suspend fun getListenedSongsByUploader(uploaderId: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getListenedSongByUploader(uploaderId)
        }
    }

    suspend fun getRecentlyPlayedSongsByUploader(uploaderId: Int, limit: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getRecentlyPlayedSongsByUploader(uploaderId, limit)
        }
    }

    suspend fun toggleLikeSong(songId: Int, liked: Boolean) {
        withContext(Dispatchers.IO) {
            songDao.toggleLikeSong(songId, liked)
        }
    }

    suspend fun isSongLiked(songId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            songDao.isSongLiked(songId)
        }
    }

    suspend fun updateSongInfo(songId: Int, imageUri: String?, artist: String, title: String) {
        songDao.updateSongInfo(songId, imageUri, artist, title)
    }

    suspend fun getNewSongsByUploader(uploaderId: Int, limit: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getNewSongsByUploader(uploaderId, limit)
        }
    }

    suspend fun getListenedSongsCount(uploaderId: Int): Int {
        return withContext(Dispatchers.IO) {
            songDao.getListenedSongsCount(uploaderId)
        }
    }
    
    suspend fun addSecondsPlayed(songId: Int, seconds: Int) {
        Log.d("DEBUG IN ADDSECONDSPLAYED","JUMLAH DETIK: $seconds")
        withContext(Dispatchers.IO) {
            songDao.addSecondsPlayed(songId, seconds)
        }
    }
    
    suspend fun getMostListenedSongs(uploaderId: Int, limit: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getMostListenedSongsByUploader(uploaderId, limit)
        }
    }
    
    suspend fun getTopArtistsByListeningTime(uploaderId: Int, limit: Int): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            val artistStats = songDao.getTopArtistsByListeningTime(uploaderId, limit)
            artistStats.associate { it.artist to it.totalSeconds }
        }
    }
    
    suspend fun getTotalListeningTime(uploaderId: Int): Int {
        return withContext(Dispatchers.IO) {
            songDao.getTotalListeningTime(uploaderId)
        }
    }
    
    suspend fun getMaxStreak(uploaderId: Int): Int {
        return withContext(Dispatchers.IO) {
            songDao.getMaxStreakByUploader(uploaderId)
        }
    }

    suspend fun getDayStreak(uploaderId: Int, limit: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getdayStreakListByUploader(uploaderId, limit)
        }
    }
    
    fun formatListeningTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        
        return when {
            hours > 0 -> "$hours h $minutes m"
            minutes > 0 -> "$minutes m $remainingSeconds s"
            else -> "$remainingSeconds s"
        }
    }
}
