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
import java.util.Locale

fun durationToSeconds(duration: String): Int {
    val parts = duration.split(":")
    if (parts.size != 2) return 0
    val minutes = parts[0].toIntOrNull() ?: 0
    val seconds = parts[1].toIntOrNull() ?: 0
    return minutes * 60 + seconds
}

fun secondsToDuration(seconds: Int): String {
    if (seconds < 0) {
        return "00:00"
    }

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return if (hours > 0) {
        String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, remainingSeconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, remainingSeconds)
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

class ChartViewModel : ViewModel() {

    private var _chartSongs = mutableStateOf<List<Song>>(emptyList())
    val chartSongs: State<List<Song>> get() = _chartSongs

    private var currentCountry by mutableStateOf("")

    private var _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private var _title = mutableStateOf("")
    private var _subtitle = mutableStateOf("")
    private var _description = mutableStateOf("")
    private var _color = mutableStateOf("")
    private var _totalDuration = mutableStateOf("")

    val title: State<String> get() = _title
    val subtitle: State<String> get() = _subtitle
    val description: State<String> get() = _description
    val color: State<String> get() = _color
    val totalDuration: State<String> get() = _totalDuration

    private fun loadChart(chartType: String, country: String = "") {
        Log.d("ChartViewModel", "Fetching chart for type: $chartType, country: $country")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chartSongsResponse = when (chartType) {
                    "global" -> RetrofitClient.api.getTopSongsGlobal()
                    "country" -> RetrofitClient.api.getTopSongsCountry(country)
                //  "foryou" -> // nanti ye
                    else -> emptyList()
                }
                _chartSongs.value = chartSongsResponse.map { it.toSong() }

                _totalDuration.value = secondsToDuration(
                    _chartSongs.value.sumOf { it.duration }
                )
            } catch (e: Exception) {
                Log.e("ChartViewModel", "Error fetching chart songs", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setChartType(chartType: String, country: String = "") {

        currentCountry = country
        loadChart(chartType, country)

        when (chartType) {
            "global" -> {
                _title.value = "Top 50"
                _subtitle.value = "Global"
                _description.value =
                    "Your daily update of the most played tracks right now - Global."
                _color.value = "blue"
            }

            "country" -> {
                _title.value = "Top 10"
                _subtitle.value = country
                _description.value =
                    "Your daily update of the most played tracks right now - $country."
                _color.value = "red"
            }

            "foryou" -> {
                _title.value = "For You"
                _subtitle.value = "Personalized"
                _description.value = "Your personalized chart based on your listening habits."
                _color.value = "green"
            }
        }
    }
}

@Composable
fun getChartViewModel(): ChartViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}