package com.android.purrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.android.purrytify.data.local.AppDatabase
import com.android.purrytify.data.local.repositories.SongRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.android.purrytify.data.local.initializer.SongInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val songRepository = SongRepository(database.songDao())

        initializeSongData(songRepository)

        setContent {
            PurrytifyApp(songRepository)
        }
    }

    private fun initializeSongData(songRepository: SongRepository) {
        lifecycleScope.launch {
            SongInitializer.initializeSongs(songRepository, applicationContext)
        }
    }
}