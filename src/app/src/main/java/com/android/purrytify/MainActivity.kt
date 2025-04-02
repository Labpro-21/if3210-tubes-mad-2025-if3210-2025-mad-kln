package com.android.purrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.android.purrytify.data.local.AppDatabase
import com.android.purrytify.data.local.repositories.SongRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.android.purrytify.data.local.entities.Song

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val songRepository = SongRepository(database.songDao())

        clearDatabaseAndInsertMockData(database, songRepository)

        setContent {
            PurrytifyApp(songRepository)
        }
    }

    private fun clearDatabaseAndInsertMockData(database: AppDatabase, songRepository: SongRepository) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.clearAllTables()

            val mockSongs = listOf(
                Song(title = "Song 1", artist = "Artist 1", imageUri = "image_uri_1", songUri = "song_uri_1"),
                Song(title = "Song 2", artist = "Artist 2", imageUri = "image_uri_2", songUri = "song_uri_2"),
                Song(title = "Song 3", artist = "Artist 3", imageUri = "image_uri_3", songUri = "song_uri_3"),
            )

            songRepository.insertSongs(mockSongs)
        }
    }
}