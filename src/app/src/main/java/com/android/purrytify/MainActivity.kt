package com.android.purrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.android.purrytify.data.local.AppDatabase
import com.android.purrytify.data.local.repositories.SongRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.android.purrytify.data.local.initializer.SongInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        val database = AppDatabase.getDatabase(applicationContext)
        val songRepository = SongRepository(database.songDao())

        var isReady = false

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            initializeSongData(songRepository)
            isReady = true
        }

        setContent {
            PurrytifyApp(songRepository, this@MainActivity)
        }
    }

    private suspend fun initializeSongData(songRepository: SongRepository) {
        SongInitializer.initializeSongs(songRepository, applicationContext)
    }
}
