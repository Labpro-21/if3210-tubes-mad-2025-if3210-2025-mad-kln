@file:Suppress("CommentSpacing", "RedundantVisibilityModifier", "unused", "SpellCheckingInspection")

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
    val duration: Int = 0,      // dalam detik
    val uploaderId: Int = 0,    // kalau 0 berarti lagu online
    val country: String = "",   // tergantung data online | kalau local sesuai user.location

    // khusus lagu local
    val liked: Boolean = false,
    val uploadDate: String = "",      // ini tadinya null gw ubah jadi ""
    val lastPlayedDate: String = "",  // ini tadinya null gw ubah jadi ""
    val isDownloaded: Boolean = false,

    // khusus lagu online
    var rank: Int = 0,
)
