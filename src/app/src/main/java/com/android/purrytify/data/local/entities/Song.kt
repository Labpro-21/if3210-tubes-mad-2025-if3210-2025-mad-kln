package com.android.purrytify.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val imageUri: String,
    val audioUri: String,
    val uploaderId: Int = 0,
    val liked: Boolean = false,
    val uploadDate: String? = null,
    val lastPlayedDate: String? = null
)
