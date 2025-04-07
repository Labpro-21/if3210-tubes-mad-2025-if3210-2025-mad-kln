package com.android.purrytify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.purrytify.data.local.entities.Song

@Dao
interface SongDao {
    @Insert
    suspend fun insertSong(song: Song)

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Int): Song?

    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongs(): List<Song>

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: Int)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("UPDATE songs SET lastPlayedDate = :date WHERE id = :songId")
    suspend fun updateLastPlayedDate(songId: Int, date: String)

    @Query("SELECT * FROM songs WHERE uploaderId = :uploaderId")
    suspend fun getSongsByUploader(uploaderId: Int): List<Song>

    @Query("SELECT * FROM songs WHERE uploaderId = :uploaderId AND liked = 1")
    suspend fun getLikedSongsByUploader(uploaderId: Int): List<Song>

    @Query("SELECT * FROM songs WHERE uploaderId = :uploaderId ORDER BY uploadDate DESC LIMIT :limit")
    suspend fun getRecentSongsByUploader(uploaderId: Int, limit: Int): List<Song>
}

