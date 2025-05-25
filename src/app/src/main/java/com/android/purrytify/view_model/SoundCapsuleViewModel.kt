package com.android.purrytify.view_model

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.PlaybackLog
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.PlaybackLogRepository
import com.android.purrytify.data.local.repositories.SongRepository
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class DailyPlayback(val dayOfMonth: Int, val minutesListened: Int)

class SoundCapsuleViewModel(
    private val songRepository: SongRepository = RepositoryProvider.getSongRepository(),
    private val playbackLogRepository: PlaybackLogRepository = RepositoryProvider.getPlaybackLogRepository()
) : ViewModel() {

    var songs by mutableStateOf<List<Song>>(emptyList())
        private set

    var timeListened by mutableStateOf("0 Minutes")
        private set

    var totalMinutesInMonth by mutableIntStateOf(0)
        private set
    var dailyAverageMinutesInMonth by mutableIntStateOf(0)
        private set
    var dailyPlaybackData by mutableStateOf<List<DailyPlayback>>(emptyList())
        private set
    var currentMonthYearForModal by mutableStateOf("")
        private set

    var topArtistName by mutableStateOf("")
        private set
    var topArtistImageUri by mutableStateOf("")
        private set
    var topArtistData by mutableStateOf<List<TopArtistInfo>>(emptyList())
        private set
    var artistCount by mutableIntStateOf(0)
        private set

    var topSongTitle by mutableStateOf("")
        private set
    var topSongImageUri by mutableStateOf("")
        private set
    var topSongData by mutableStateOf<List<TopSongInfo>>(emptyList())
        private set
    var songCount by mutableIntStateOf(0)
        private set

    var maxStreakSongTitle by mutableStateOf("")
        private set
    var maxStreakArtistName by mutableStateOf("")
        private set
    var maxStreakImageUri by mutableStateOf("")
        private set
    var maxStreakCount by mutableIntStateOf(0)
        private set
    var maxStreakDateRange by mutableStateOf("N/A")
        private set

    private val calendar = Calendar.getInstance()
    var currentMonthDisplay by mutableStateOf("")
        private set
    var hasNextMonth by mutableStateOf(false)
        private set
    var hasPreviousMonth by mutableStateOf(false)
        private set

    init {
        updateCurrentMonthDisplay()
    }

    private fun updateCurrentMonthDisplay() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        currentMonthDisplay = monthFormat.format(calendar.time)
        currentMonthYearForModal = currentMonthDisplay
        
        val now = Calendar.getInstance()
        hasNextMonth = !(calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH))

        viewModelScope.launch {
            val userId = songs.firstOrNull()?.uploaderId ?: 0
            val oldestYearMonth = playbackLogRepository.getOldestLogYearMonth(userId)
            
            if (oldestYearMonth != null) {
                val oldestDate = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(oldestYearMonth)
                val currentDate = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
                val currentCalendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(currentDate)!!
                }
                
                hasPreviousMonth = oldestDate?.before(currentCalendar.time) ?: false
                
            } else {
                hasPreviousMonth = false
            }
        }
    }

    fun navigateToPreviousMonth() {
        if (!hasPreviousMonth) return
        calendar.add(Calendar.MONTH, -1)
        updateCurrentMonthDisplay()
        fetchSongs(userId = songs.firstOrNull()?.uploaderId ?: 0)
    }

    fun navigateToNextMonth() {
        val now = Calendar.getInstance()
        if (calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
            return
        }
        calendar.add(Calendar.MONTH, 1)
        updateCurrentMonthDisplay()
        fetchSongs(userId = songs.firstOrNull()?.uploaderId ?: 0)
    }

    fun fetchSongs(userId: Int) {
        viewModelScope.launch {
            if (songs.isEmpty()) {
                songs = songRepository.getSongsByUploader(userId)
            }

            val currentYearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            val logs = playbackLogRepository.getLogsByYearMonth(currentYearMonth, userId)
            
            timeListened = getTimeListenedForCard(logs)
            Log.d("SoundCapsuleViewModel", "Total Duration Played: $timeListened")

            processPlaybackLogsForMonthDetail(logs)

            val (artistCountVal, topArtistDataVal) = getTopArtistData(logs, songs, 5)
            if (topArtistDataVal.isNotEmpty()) {
                topArtistName = topArtistDataVal[0].artist
                topArtistImageUri = topArtistDataVal[0].imageUri
            } else {
                topArtistName = ""
                topArtistImageUri = ""
            }
            topArtistData = topArtistDataVal
            artistCount = artistCountVal

            val (songCountVal, songDataVal) = getTopSongData(logs, songs, 5)
            if (songDataVal.isNotEmpty()) {
                topSongTitle = songDataVal[0].title
                topSongImageUri = songDataVal[0].imageUri
            } else {
                topSongTitle = ""
                topSongImageUri = ""
            }
            songCount = songCountVal
            topSongData = songDataVal

            val maxStreakInfo = calculateMaxStreak(logs, songs)
            if (maxStreakInfo != null) {
                maxStreakSongTitle = maxStreakInfo.songTitle
                maxStreakArtistName = maxStreakInfo.artistName
                maxStreakImageUri = maxStreakInfo.imageUri
                maxStreakCount = maxStreakInfo.streakCount
                maxStreakDateRange = maxStreakInfo.dateRange
            } else {
                maxStreakSongTitle = ""
                maxStreakArtistName = ""
                maxStreakImageUri = ""
                maxStreakCount = 0
                maxStreakDateRange = "N/A"
            }
        }
    }

    private fun processPlaybackLogsForMonthDetail(logs: List<PlaybackLog>) {
        val totalMsInMonth = logs.sumOf { it.durationMs }
        totalMinutesInMonth = (totalMsInMonth / (1000 * 60)).toInt()

        val logsByDay = logs.groupBy { it.dayOfMonth }
        val playbackByDay = mutableListOf<DailyPlayback>()
        Log.d("SoundCapsuleVM_ChartData", "--- Daily Playback Pairs (Day, Minutes) ---")
        for ((day, dayLogs) in logsByDay) {
            val totalMinutesForDay = (dayLogs.sumOf { it.durationMs } / (1000 * 60)).toInt()
            playbackByDay.add(DailyPlayback(dayOfMonth = day, minutesListened = totalMinutesForDay))
            Log.d("SoundCapsuleVM_ChartData", "Day: $day, Minutes: $totalMinutesForDay")
        }
        dailyPlaybackData = playbackByDay.sortedBy { it.dayOfMonth }
        Log.d("SoundCapsuleVM_ChartData", "--- End of Daily Playback Pairs ---")

        if (logs.isNotEmpty()) {
            val distinctDaysPlayed = logs.map { it.dayOfMonth }.distinct().size
            dailyAverageMinutesInMonth = if (distinctDaysPlayed > 0) {
                (totalMinutesInMonth.toDouble() / distinctDaysPlayed).roundToInt()
            } else {
                0
            }
        } else {
            dailyAverageMinutesInMonth = 0
        }

        Log.d("SoundCapsuleVM", "TotalMin: $totalMinutesInMonth, AvgMin: $dailyAverageMinutesInMonth, DailyData: ${dailyPlaybackData.size} entries")
    }

    private fun exportToCSV(): String {
        val csvBuilder = StringBuilder()
        
        csvBuilder.appendLine("Sound Capsule Data for $currentMonthDisplay")
        csvBuilder.appendLine()
        
        csvBuilder.appendLine("Overall Statistics")
        csvBuilder.appendLine("Total Time Listened,$timeListened")
        csvBuilder.appendLine("Total Minutes in Month,$totalMinutesInMonth")
        csvBuilder.appendLine("Daily Average Minutes,$dailyAverageMinutesInMonth")
        csvBuilder.appendLine("Total Artists,$artistCount")
        csvBuilder.appendLine("Total Songs,$songCount")
        csvBuilder.appendLine()
        
        csvBuilder.appendLine("Daily Playback Data")
        csvBuilder.appendLine("Day,Minutes Listened")
        dailyPlaybackData.forEach { data ->
            csvBuilder.appendLine("${data.dayOfMonth},${data.minutesListened}")
        }
        csvBuilder.appendLine()
        
        csvBuilder.appendLine("Top Artists")
        csvBuilder.appendLine("Rank,Artist Name")
        topArtistData.forEach { artist ->
            csvBuilder.appendLine("${artist.rank},${artist.artist}")
        }
        csvBuilder.appendLine()
        
        csvBuilder.appendLine("Top Songs")
        csvBuilder.appendLine("Rank,Title,Artist,Play Count")
        topSongData.forEach { song ->
            csvBuilder.appendLine("${song.rank},${song.title},${song.artist},${song.playCount}")
        }
        csvBuilder.appendLine()
        
        csvBuilder.appendLine("Longest Listening Streak")
        csvBuilder.appendLine("Song,${maxStreakSongTitle}")
        csvBuilder.appendLine("Artist,${maxStreakArtistName}")
        csvBuilder.appendLine("Streak Count,${maxStreakCount}")
        csvBuilder.appendLine("Date Range,${maxStreakDateRange}")
        
        return csvBuilder.toString()
    }

    fun saveToCSV(context: Context): String {
        val csvData = exportToCSV()
        val fileName = "sound_capsule_${currentMonthDisplay.replace(" ", "_")}.csv"
        
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(csvData)
                    }
                }
                "CSV file saved successfully to Downloads/$fileName"
            } ?: throw IOException("Failed to create file")
        } catch (e: Exception) {
            "Error saving CSV file: ${e.message}"
        }
    }
}

@Composable
fun getSoundCapsuleViewModel(): SoundCapsuleViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}

fun getTimeListenedForCard(logs: List<PlaybackLog>): String {
    val totalDurationSeconds = logs.sumOf { it.durationMs } / 1000
    val minutes = totalDurationSeconds / 60
    val seconds = totalDurationSeconds % 60

    return when {
        totalDurationSeconds == 0L -> "0 Minutes"
        minutes == 0L -> "$seconds Seconds"
        else -> "$minutes Minutes"
    }
}

data class TopArtistInfo(
    val rank: Int,
    val artist: String,
    val imageUri: String
)

fun getTopArtistData(logs: List<PlaybackLog>, songs: List<Song>, limit: Int): Pair<Int, List<TopArtistInfo>> {
    val artistListeningStatsMs = mutableMapOf<String, Long>()

    logs.forEach { log ->
        val artist = log.artistName
        artistListeningStatsMs[artist] = artistListeningStatsMs.getOrDefault(artist, 0L) + log.durationMs
    }

    val sortedArtists = artistListeningStatsMs.entries.sortedByDescending { it.value }

    val topArtistsInfo = sortedArtists.take(limit).mapIndexed { index, entry ->
        val artistName = entry.key
        val imageUri = songs.firstOrNull { it.artist == artistName }?.imageUri ?: ""
        TopArtistInfo(
            rank = index + 1,
            artist = artistName,
            imageUri = imageUri
        )
    }
    val totalDistinctArtists = logs.map { it.artistName }.distinct().size
    return Pair(totalDistinctArtists, topArtistsInfo)
}

data class TopSongInfo(
    val rank: Int,
    val title: String,
    val imageUri: String,
    val artist: String,
    val playCount: Int
)

fun getTopSongData(logs: List<PlaybackLog>, songs: List<Song>, limit: Int): Pair<Int, List<TopSongInfo>> {
    val songStats = logs.groupBy { it.songId }.mapValues { entry ->
        val songLogs = entry.value
        val totalDurationMs = songLogs.sumOf { it.durationMs }
        val playCount = songLogs.size
        val firstLog = songLogs.first()
        Pair(totalDurationMs, playCount) to firstLog
    }

    val sortedSongStats = songStats.entries.sortedWith(
        compareByDescending<Map.Entry<String, Pair<Pair<Long, Int>, PlaybackLog>>> { it.value.first.second }
            .thenByDescending { it.value.first.first }
    )

    val topSongsInfo = sortedSongStats.take(limit).mapIndexed { index, entry ->
        val songId = entry.key
        val (_, playCount) = entry.value.first
        val representativeLog = entry.value.second

        val imageUri = songs.firstOrNull { it.id.toString() == songId }?.imageUri ?: ""

        TopSongInfo(
            rank = index + 1,
            title = representativeLog.songTitle,
            imageUri = imageUri,
            artist = representativeLog.artistName,
            playCount = playCount
        )
    }
    val totalDistinctSongs = logs.map { it.songId }.distinct().size
    return Pair(totalDistinctSongs, topSongsInfo)
}

data class MaxStreakInfo(
    val songTitle: String,
    val artistName: String,
    val imageUri: String,
    val streakCount: Int,
    val dateRange: String
)

fun calculateMaxStreak(logs: List<PlaybackLog>, songs: List<Song>): MaxStreakInfo? {
    if (logs.isEmpty()) return null

    val songLogsBySongId = logs.groupBy { it.songId }
    var overallMaxStreak = 0
    var bestSongIdForStreak: String? = null
    var streakStartDate: Calendar? = null
    var streakEndDate: Calendar? = null

    val logYearMonth = logs.first().yearMonth
    val year = logYearMonth.substring(0, 4).toInt()
    val month = logYearMonth.substring(5, 7).toInt()


    for ((songId, logsForSong) in songLogsBySongId) {
        if (logsForSong.isEmpty()) continue

        val distinctDaysPlayed = logsForSong.map { it.dayOfMonth }.distinct().sorted()
        if (distinctDaysPlayed.isEmpty()) continue

        var currentStreak = 0
        var currentStreakStartDay: Int
        var songMaxStreak = 0
        var songStreakStartDay = 0
        var songStreakEndDay = 0

        for (i in distinctDaysPlayed.indices) {
            val day = distinctDaysPlayed[i]
            if (i > 0 && day == distinctDaysPlayed[i-1] + 1) {
                currentStreak++
            } else {
                currentStreak = 1
            }
            currentStreakStartDay = if(currentStreak == 1) day else distinctDaysPlayed[i - currentStreak + 1]


            if (currentStreak > songMaxStreak) {
                songMaxStreak = currentStreak
                songStreakStartDay = currentStreakStartDay
                songStreakEndDay = day
            }
        }

        if (songMaxStreak > overallMaxStreak) {
            overallMaxStreak = songMaxStreak
            bestSongIdForStreak = songId

            val calStart = Calendar.getInstance()
            calStart.set(year, month - 1, songStreakStartDay)
            streakStartDate = calStart

            val calEnd = Calendar.getInstance()
            calEnd.set(year, month - 1, songStreakEndDay)
            streakEndDate = calEnd
        }
    }

    if (bestSongIdForStreak != null && streakStartDate != null && streakEndDate != null) {
        val representativeLog = logs.firstOrNull { it.songId == bestSongIdForStreak }
        val songDetails = songs.firstOrNull { it.id.toString() == bestSongIdForStreak }

        val startDateFormatted = SimpleDateFormat("MMM d", Locale.getDefault()).format(
            streakStartDate.time)
        val endDateFormatted = SimpleDateFormat("d, yyyy", Locale.getDefault()).format(streakEndDate.time)
        val dateRangeStr = if (overallMaxStreak == 1) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(streakStartDate.time)
        } else {
            "$startDateFormatted–$endDateFormatted"
        }


        return MaxStreakInfo(
            songTitle = representativeLog?.songTitle ?: "Unknown Song",
            artistName = representativeLog?.artistName ?: "Unknown Artist",
            imageUri = songDetails?.imageUri ?: "",
            streakCount = overallMaxStreak,
            dateRange = dateRangeStr
        )
    }
    return null
}
