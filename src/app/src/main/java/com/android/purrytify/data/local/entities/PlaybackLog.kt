package com.android.purrytify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_logs")
data class PlaybackLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: String,
    val artistId: String,
    val artistName: String,
    val songTitle: String,
    val durationMs: Long,
    val timestamp: Long,
    val yearMonth: String,
    val dayOfMonth: Int
) 