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
        Log.d("UPDATE DATE","SONG ID: $songId, DATE: $date")
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
}
