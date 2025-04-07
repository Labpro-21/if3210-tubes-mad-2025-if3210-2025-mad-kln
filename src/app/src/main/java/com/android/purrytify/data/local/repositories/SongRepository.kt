package com.android.purrytify.data.local.repositories

import android.util.Log
import com.android.purrytify.data.local.dao.SongDao
import com.android.purrytify.data.local.entities.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SongRepository(private val songDao: SongDao) {

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun insertSong(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            val finalSong = if (song.uploadDate == null) {
                song.copy(uploadDate = getCurrentDate())
            } else {
                song
            }

            songDao.insertSong(finalSong)
            Log.d("Database", "Inserted Song: $finalSong")
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

    suspend fun updateLastPlayedDate(songId: Int, date: String = getCurrentDate()) {
        withContext(Dispatchers.IO) {
            songDao.updateLastPlayedDate(songId, date)
        }
    }


    suspend fun getSongsByUploader(uploaderId: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getSongsByUploader(uploaderId)
        }
    }

    suspend fun getLikedSongsByUploader(uploaderId: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getLikedSongsByUploader(uploaderId)
        }
    }

    suspend fun getRecentSongsByUploader(uploaderId: Int, limit: Int): List<Song> {
        return withContext(Dispatchers.IO) {
            songDao.getRecentSongsByUploader(uploaderId, limit)
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
}
