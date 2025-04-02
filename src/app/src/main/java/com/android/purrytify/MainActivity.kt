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
                Song(0,"Song 1", "Author 1", "image_uri_1", "song_uri_1"),
                Song(1, "Song 2", "Author 2", "image_uri_2", "song_uri_2"),
                Song(2, "Song 3", "Author 3", "image_uri_3", "song_uri_3")
            )

            songRepository.insertSongs(mockSongs)
        }
    }
}