package com.android.purrytify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.android.purrytify.data.local.AppDatabase
import com.android.purrytify.data.local.repositories.*
import com.android.purrytify.data.local.initializer.SongInitializer
import androidx.lifecycle.lifecycleScope
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.network.OnlineSongResponse
import com.android.purrytify.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private var latestIntent: Intent? = null

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        latestIntent = intent
        Log.d("intent","test")
    }

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

        setContent {
            PurrytifyApp(this@MainActivity, latestIntent ?: intent)
        }
    }
}
