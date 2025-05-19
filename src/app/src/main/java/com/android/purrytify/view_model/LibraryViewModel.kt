package com.android.purrytify.view_model

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.datastore.TokenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch


class LibraryViewModel(
    private val songRepository: SongRepository = RepositoryProvider.getSongRepository()
) : ViewModel() {

    private var allSongs by mutableStateOf<List<Song>>(emptyList())
    private var likedSongs by mutableStateOf<List<Song>>(emptyList())
    private var downloadedSongs by mutableStateOf<List<Song>>(emptyList())

    var tab by mutableStateOf("all")
        private set

    var activeSongs by mutableStateOf<List<Song>>(emptyList())
        private set

    init {
        updateActiveSongs()
    }

    fun fetchAllSongs(userId: Int) {
        viewModelScope.launch {
            delay(100)
            allSongs = songRepository.getSongsByUploader(userId) ?: emptyList()
            updateActiveSongs()
        }
    }

    fun fetchLikedSongs(userId: Int) {
        viewModelScope.launch {
            delay(100)
            likedSongs = songRepository.getLikedSongsByUploader(userId) ?: emptyList()
            updateActiveSongs()
        }
    }

    fun fetchLibrary(userId: Int, searchQuery: String = "") {
        Log.d("LibraryViewModel", "fetchLibrary called with userId: $userId")
        viewModelScope.launch {
            delay(100)

            allSongs = songRepository.getSongsByUploader(userId, searchQuery) ?: emptyList()
            likedSongs = songRepository.getLikedSongsByUploader(userId, searchQuery) ?: emptyList()
            downloadedSongs = songRepository.getDownloadedSongsByUploader(userId, searchQuery) ?: emptyList()
            updateActiveSongs()
        }
    }

    fun switchTab(to: String) {
        tab = to
        updateActiveSongs()
    }

    private fun updateActiveSongs() {
        if (tab == "downloaded") {
            activeSongs = downloadedSongs
        } else if (tab == "liked") {
            activeSongs = likedSongs
        } else {
            activeSongs = allSongs
        }
    }
}


@Composable
fun getLibraryViewModel(): LibraryViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}

