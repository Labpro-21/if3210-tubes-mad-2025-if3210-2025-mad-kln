package com.android.purrytify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.android.purrytify.data.local.AppDatabase
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.initializer.SongInitializer
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.data.local.repositories.UserRepository
import com.android.purrytify.network.OnlineSongResponse
import com.android.purrytify.network.RetrofitClient
import com.android.purrytify.view_model.durationToSeconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var latestIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        val database = AppDatabase.getDatabase(applicationContext)
        val songRepository = SongRepository(database.songDao())
        val userRepository = UserRepository(database.userDao())
        RepositoryProvider.init(songRepository, userRepository)

        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            try {
                SongInitializer.initializeSongs(songRepository, applicationContext)
                isReady = true
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing songs: ${e.message}", e)
                isReady = true
            }
        }

        handleIntent(intent)

        setContent {
            PurrytifyApp(this@MainActivity, latestIntent ?: intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        latestIntent = intent
        intent?.let {
            handleIntent(it)
        }
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null) {
            val songId = data.getQueryParameter("songId")
            if (songId != null) {
                Log.d("DeepLink", "Play song with ID: $songId")

                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.api.getSongById(songId.toInt())
                        val song = response.toSong()
                        SharedSongState.setSong(song)
                        Log.d("DeepLink", "Fetched song: ${song.title}")
                    } catch (e: Exception) {
                        Log.e("DeepLink", "Failed to fetch song", e)
                    }
                }
            }
        }
    }
}

fun OnlineSongResponse.toSong(): Song {
    return Song(
        id = id,
        title = title,
        artist = artist,
        imageUri = artwork,
        audioUri = url,
        duration = durationToSeconds(duration),
        country = country,
        rank = rank
    )
}

object SharedSongState {
    private val _deepLinkedSong = MutableStateFlow<Song?>(null)
    val deepLinkedSong: StateFlow<Song?> = _deepLinkedSong

    fun setSong(song: Song) {
        _deepLinkedSong.value = song
    }

    fun clear() {
        _deepLinkedSong.value = null
    }
}
