package com.android.purrytify.view_model

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.RepositoryProvider
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

    private var _userId = mutableStateOf(0)
    val userId: State<Int> get() = _userId

    private val _globalChartSongs = mutableStateOf<List<Song>>(emptyList())
    private val _countryChartSongs = mutableStateOf<List<Song>>(emptyList())
    private val _forYouChartSongs = mutableStateOf<List<Song>>(emptyList())

    private var _chartSongs = mutableStateOf<List<Song>>(emptyList())
    val chartSongs: State<List<Song>> get() = _chartSongs

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

    private var currentDisplayChartType: String? = null
    private var loadedCountryForCountryChart: String? = null
    private var loadedCountryForForYouChart: String? = null
    private var userIdForForYouChart: Int? = null

    fun setUserId(id: Int) {
        if (id != 0 && _userId.value != id) {
            val oldUserId = _userId.value
            _userId.value = id
            if (_forYouChartSongs.value.isNotEmpty() && userIdForForYouChart != id) {
                _forYouChartSongs.value = emptyList()
                userIdForForYouChart = null 
                loadedCountryForForYouChart = null
                if (currentDisplayChartType == "foryou") {
                    _chartSongs.value = emptyList()
                    _totalDuration.value = "00:00"
                }
            }
        } else if (id == 0 && _userId.value != 0) { 
             _userId.value = id
             _forYouChartSongs.value = emptyList()
             userIdForForYouChart = null
             loadedCountryForForYouChart = null
             if (currentDisplayChartType == "foryou") {
                 _chartSongs.value = emptyList()
                 _totalDuration.value = "00:00"
             }
        }
    }

    fun fetchAllChartsInitial(userCountry: String) {
        if (_userId.value == 0 && userCountry.isBlank()) {
            return
        }
        if (_globalChartSongs.value.isEmpty()) {
            loadChart("global")
        }
        if (userCountry.isNotBlank() && (_countryChartSongs.value.isEmpty() || loadedCountryForCountryChart != userCountry)) {
            loadChart("country", userCountry)
        }
        if (_userId.value != 0 && (_forYouChartSongs.value.isEmpty() || loadedCountryForForYouChart != userCountry || userIdForForYouChart != _userId.value)) {
            loadChart("foryou", userCountry)
        }
    }

    private fun loadChart(chartTypeToLoad: String, countryForApi: String = "") {
        if (chartTypeToLoad == "foryou" && _userId.value == 0) {
            _isLoading.value = false
            return
        }
        if (chartTypeToLoad == "country" && countryForApi.isBlank()) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chartSongsResponse = when (chartTypeToLoad) {
                    "global" -> RetrofitClient.api.getTopSongsGlobal().map { it.toSong() }
                    "country" -> RetrofitClient.api.getTopSongsCountry(countryForApi).map { it.toSong() }
                    "foryou" -> getForYouSongs(countryForApi, _userId.value)
                    else -> emptyList()
                }.sortedBy { it.rank }

                when (chartTypeToLoad) {
                    "global" -> _globalChartSongs.value = chartSongsResponse
                    "country" -> {
                        _countryChartSongs.value = chartSongsResponse
                        loadedCountryForCountryChart = countryForApi
                    }
                    "foryou" -> {
                        _forYouChartSongs.value = chartSongsResponse
                        loadedCountryForForYouChart = countryForApi
                        userIdForForYouChart = _userId.value
                    }
                }
                if (currentDisplayChartType == chartTypeToLoad) {
                    _chartSongs.value = chartSongsResponse
                    _totalDuration.value = secondsToDuration(chartSongsResponse.sumOf { it.duration })
                }
            } catch (e: Exception) {
                when (chartTypeToLoad) {
                    "global" -> _globalChartSongs.value = emptyList()
                    "country" -> { _countryChartSongs.value = emptyList(); loadedCountryForCountryChart = null }
                    "foryou" -> { _forYouChartSongs.value = emptyList(); loadedCountryForForYouChart = null; userIdForForYouChart = null }
                }
                if (currentDisplayChartType == chartTypeToLoad) {
                    _chartSongs.value = emptyList()
                    _totalDuration.value = "00:00"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setChartType(newChartType: String, newCountryParameter: String = "") {
        currentDisplayChartType = newChartType
        var shouldFetch = false
        var targetList: List<Song> = emptyList()

        when (newChartType) {
            "global" -> {
                if (_globalChartSongs.value.isEmpty()) {
                    shouldFetch = true
                }
                targetList = _globalChartSongs.value
            }
            "country" -> {
                if (newCountryParameter.isNotBlank()) {
                    if (_countryChartSongs.value.isEmpty() || loadedCountryForCountryChart != newCountryParameter) {
                        shouldFetch = true
                    }
                    targetList = _countryChartSongs.value
                } else {
                    targetList = emptyList()
                     _chartSongs.value = emptyList()
                     _totalDuration.value = "00:00"
                }
            }
            "foryou" -> {
                if (_userId.value != 0) {
                    if (_forYouChartSongs.value.isEmpty() || userIdForForYouChart != _userId.value || loadedCountryForForYouChart != newCountryParameter) {
                        shouldFetch = true
                    }
                    targetList = _forYouChartSongs.value
                } else {
                     targetList = emptyList()
                     _chartSongs.value = emptyList()
                     _totalDuration.value = "00:00"
                }
            }
            else -> {
                _chartSongs.value = emptyList()
                _totalDuration.value = "00:00"
            }
        }

        if (shouldFetch) {
            loadChart(newChartType, newCountryParameter)
        } else {
            _chartSongs.value = targetList
            _totalDuration.value = secondsToDuration(targetList.sumOf { it.duration })
        }
        
        when (newChartType) {
            "global" -> {
                _title.value = "Top 50"
                _subtitle.value = "Global"
                _description.value = "Your daily update of the most played tracks right now - Global."
                _color.value = "blue"
            }
            "country" -> {
                _title.value = "Top 10"
                _subtitle.value = newCountryParameter.ifEmpty { loadedCountryForCountryChart ?: "Country" }
                _description.value = "Your daily update of the most played tracks right now - ${_subtitle.value}."
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

suspend fun getForYouSongs(country: String, userId: Int): List<Song> {
    if (userId == 0) return emptyList()

    val globalList = RetrofitClient.api.getTopSongsGlobal().shuffled()
    val countryList = if (country.isNotBlank()) RetrofitClient.api.getTopSongsCountry(country).shuffled() else emptyList()

    val songRepository = RepositoryProvider.getSongRepository()
    val recentlyPlayed = songRepository.getRecentlyPlayedSongsByUploader(userId, 5) ?: emptyList()
    val liked = songRepository.getLikedSongsByUploader(userId) ?: emptyList()

    val seenTitles = mutableSetOf<String>()
    val result = mutableListOf<Song>()

    var recentlyAdded = 0
    for (song in recentlyPlayed) {
        if (seenTitles.add(song.title)) {
            result.add(song)
            recentlyAdded++
            if (recentlyAdded >= 3) break
        }
    }

    var likedAdded = 0
    for (song in liked) {
        if (seenTitles.add(song.title)) {
            result.add(song)
            likedAdded++
            if (likedAdded >= 2) break
        }
    }

    val combinedChartList = (globalList + countryList).distinctBy { it.title }.shuffled()
    for (songResponse in combinedChartList) {
        if (result.size >= 10) break
        val song = songResponse.toSong()
        if (seenTitles.add(song.title)) {
            result.add(song)
        }
    }
    
    val finalResult = if (result.size > 10) result.take(10) else result

    finalResult.forEachIndexed { index, song ->
        song.rank = index + 1
    }

    return finalResult
}
