package com.android.purrytify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.purrytify.data.local.entities.PlaybackLog

@Dao
interface PlaybackLogDao {
    @Insert
    suspend fun insert(log: PlaybackLog)

    @Query("SELECT * FROM playback_logs WHERE yearMonth = :yearMonth AND userId = :userId ORDER BY timestamp DESC")
    suspend fun getLogsByYearMonth(yearMonth: String, userId: Int): List<PlaybackLog>

    @Query("SELECT * FROM playback_logs WHERE songId = :songId ORDER BY timestamp DESC")
    suspend fun getLogsBySongId(songId: String): List<PlaybackLog>

    @Query("SELECT * FROM playback_logs WHERE artistId = :artistId ORDER BY timestamp DESC")
    suspend fun getLogsByArtistId(artistId: String): List<PlaybackLog>

    @Query("SELECT SUM(durationMs) FROM playback_logs WHERE yearMonth = :yearMonth")
    suspend fun getTotalDurationByYearMonth(yearMonth: String): Long

    @Query("SELECT SUM(durationMs) FROM playback_logs WHERE songId = :songId")
    suspend fun getTotalDurationBySongId(songId: String): Long

    @Query("SELECT SUM(durationMs) FROM playback_logs WHERE artistId = :artistId")
    suspend fun getTotalDurationByArtistId(artistId: String): Long
} 