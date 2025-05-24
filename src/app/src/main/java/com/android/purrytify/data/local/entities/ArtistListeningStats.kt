package com.android.purrytify.data.local.entities

import androidx.room.ColumnInfo

data class ArtistListeningStats(
    @ColumnInfo(name = "artist") val artist: String,
    @ColumnInfo(name = "totalSeconds") val totalSeconds: Int,
)