package com.android.purrytify.view_model

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.network.OnlineSongResponse
import com.android.purrytify.network.RetrofitClient
import kotlinx.coroutines.launch

fun durationToSeconds(duration: String): Int {
    val parts = duration.split(":")
    if (parts.size != 2) return 0
    val minutes = parts[0].toIntOrNull() ?: 0
    val seconds = parts[1].toIntOrNull() ?: 0
    return minutes * 60 + seconds
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

class TopSongViewModel : ViewModel() {

    private val _topSongsGlobal = mutableStateOf<List<Song>>(emptyList())
    private val _topSongsCountry = mutableStateOf<List<Song>>(emptyList())

    val topSongsGlobal: State<List<Song>> get() = _topSongsGlobal
    val topSongsCountry: State<List<Song>> get() = _topSongsCountry

    private var currentCountry by mutableStateOf("")

    private var _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    fun fetchTopSongsGlobal() {
        Log.d("TopSongs", "Fetching global top songs")

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val topSongs = RetrofitClient.api.getTopSongsGlobal()
                _topSongsGlobal.value = topSongs.map { song ->
                    Log.d("TopSongs", "Title: ${song.title}, Artist: ${song.artist}")
                    song.toSong()
                }
            } catch (e: Exception) {
                Log.e("TopSongs", "Error fetching songs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTopSongsByCountry(country: String) {
        Log.d("TopSongs", "Fetching top songs for country: $country")
        viewModelScope.launch {
            _isLoading.value = true
            currentCountry = country
            try {
                val topSongs = RetrofitClient.api.getTopSongsCountry(country)
                _topSongsCountry.value = topSongs.map { song ->
                    Log.d("TopSongs", "Title: ${song.title}, Artist: ${song.artist}")
                    song.toSong()
                }
            } catch (e: Exception) {
                Log.e("TopSongs", "Error fetching country songs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

//    init {
//        _isLoading = true
//        fetchTopSongsByCountry("ID")
//        fetchTopSongsGlobal()
//        _isLoading = false
//    }

    fun refreshTopSongs() {
        viewModelScope.launch {
            fetchTopSongsByCountry(currentCountry)
        }
    }

    fun refreshTopSongsCountry(country: String){
        viewModelScope.launch {
            fetchTopSongsByCountry(country)
        }
    }
}

@Composable
fun getTopSongViewModel(): TopSongViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}