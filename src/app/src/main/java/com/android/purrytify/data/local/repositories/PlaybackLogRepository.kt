package com.android.purrytify.data.local.repositories

import android.util.Log
import com.android.purrytify.data.local.dao.PlaybackLogDao
import com.android.purrytify.data.local.entities.PlaybackLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlaybackLogRepository(private val playbackLogDao: PlaybackLogDao) {
    private val MIN_PLAYBACK_DURATION_MS = 1000L
    private val BATCH_UPDATE_INTERVAL_MS = 10000L
    private var pendingLogs = mutableListOf<PlaybackLog>()
    private var lastBatchUpdateTime = System.currentTimeMillis()

    fun logPlayback(userId: Int, songId: String, artistId: String, artistName: String, songTitle: String, listenedMs: Long) {

        if (listenedMs < MIN_PLAYBACK_DURATION_MS) {
            return
        }

        val calendar = Calendar.getInstance()
        val yearMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val log = PlaybackLog(
            userId = userId,
            songId = songId,
            artistId = artistId,
            artistName = artistName,
            songTitle = songTitle,
            durationMs = listenedMs,
            timestamp = System.currentTimeMillis(),
            yearMonth = yearMonth,
            dayOfMonth = dayOfMonth
        )

        pendingLogs.add(log)

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBatchUpdateTime >= BATCH_UPDATE_INTERVAL_MS) {
            flushLogs()
        } else {
            Log.d("PlaybackLogRepository", "Pending log added for $songTitle, will flush later")
        }
    }

    fun flushLogs() {
        if (pendingLogs.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                pendingLogs.forEach { log ->
                    playbackLogDao.insert(log)
                    Log.d("PlaybackLogRepository", "Logged ${log.durationMs}ms for ${log.songTitle}")
                }
                pendingLogs.clear()
                lastBatchUpdateTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("PlaybackLogRepository", "Error flushing logs", e)
            }
        }
    }

    suspend fun getLogsByYearMonth(yearMonth: String, userId: Int): List<PlaybackLog> {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getLogsByYearMonth(yearMonth, userId)
        }
    }

    suspend fun getLogsBySongId(songId: String): List<PlaybackLog> {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getLogsBySongId(songId)
        }
    }

    suspend fun getLogsByArtistId(artistId: String): List<PlaybackLog> {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getLogsByArtistId(artistId)
        }
    }

    suspend fun getTotalDurationByYearMonth(yearMonth: String): Long {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getTotalDurationByYearMonth(yearMonth)
        }
    }

    suspend fun getTotalDurationBySongId(songId: String): Long {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getTotalDurationBySongId(songId)
        }
    }

    suspend fun getTotalDurationByArtistId(artistId: String): Long {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getTotalDurationByArtistId(artistId)
        }
    }

    suspend fun getOldestLogYearMonth(userId: Int): String? {
        return withContext(Dispatchers.IO) {
            playbackLogDao.getOldestLogYearMonth(userId)
        }
    }
} 