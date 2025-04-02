package com.android.purrytify.data.local.repositories

import com.android.purrytify.data.local.dao.SongDao
import com.android.purrytify.data.local.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Log


class SongRepository(private val songDao: SongDao) {

    fun insertSong(song: Song) {
        CoroutineScope(Dispatchers.IO).launch {
            songDao.insertSong(song)
            Log.d("Database", "Inserted Song: $song")
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
}
