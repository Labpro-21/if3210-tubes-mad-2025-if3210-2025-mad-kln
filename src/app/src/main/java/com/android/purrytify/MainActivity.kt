package com.android.purrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.android.purrytify.data.local.AppDatabase
import com.android.purrytify.data.local.repositories.*
import com.android.purrytify.data.local.initializer.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()

        val database = AppDatabase.getDatabase(applicationContext)
        val songRepository = SongRepository(database.songDao())
        val userRepository = UserRepository(database.userDao())

        var isReady = false

        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            SongInitializer.initializeSongs(songRepository, applicationContext)
            UserInitializer.initializeUsers(userRepository)
            isReady = true
        }

        setContent {
            PurrytifyApp(songRepository)
        }
    }
}
