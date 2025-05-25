package com.android.purrytify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.android.purrytify.MainActivity
import com.android.purrytify.R
import com.android.purrytify.controller.MediaPlayerController
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.repositories.SongRepository
import extractDominantColor
import loadBitmapFromUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import loadBitmapFromUrl
import kotlin.math.abs

class MusicService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private var lastSongId: Int? = null
    private var lastProgress: Float = 0f
    private var isSeeking = false
    private var songRepository: SongRepository? = null
    
    companion object {
        var instance: MusicService? = null
        private const val NOTIFICATION_ID = 101
        private const val PROGRESS_UPDATE_INTERVAL = 250L
        private var lastRoute: String? = null

        fun updateLastRoute(route: String?) {
            lastRoute = route
        }
    }
    
    private var progressJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        songRepository = RepositoryProvider.getSongRepository()
        
        mediaSession = MediaSessionCompat(this, "MusicService")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                MediaPlayerController.playPause()
                updatePlaybackState()
                updateNotification(true)
            }

            override fun onPause() {
                MediaPlayerController.playPause()
                updatePlaybackState()
                updateNotification(true)
            }

            override fun onSkipToNext() {
                MediaPlayerController.playNext(applicationContext)
                updatePlaybackState()
                updateNotification(true)
            }

            override fun onSkipToPrevious() {
                MediaPlayerController.playPrevious(applicationContext)
                updatePlaybackState()
                updateNotification(true)
            }
            
            override fun onSeekTo(pos: Long) {
                Log.d("MusicService", "onSeekTo: $pos")
                val duration = MediaPlayerController.totalDuration.value
                if (duration > 0) {
                    isSeeking = true
                    val progress = pos.toFloat() / duration
                    MediaPlayerController.seekTo(progress)
                    lastProgress = progress
                    updatePlaybackState()
                    updateNotification(true)
                    isSeeking = false
                }
            }
        })
        mediaSession.isActive = true
        
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()

        startPlaybackTrackingJob()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicService", "onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            "ACTION_PLAY_PAUSE" -> {
                MediaPlayerController.playPause()
                updatePlaybackState()
                updateNotification(true)
            }
            "ACTION_NEXT" -> {
                MediaPlayerController.playNext(applicationContext)
                updatePlaybackState()
                updateNotification(true)
            }
            "ACTION_PREVIOUS" -> {
                MediaPlayerController.playPrevious(applicationContext)
                updatePlaybackState()
                updateNotification(true)
            }
            "ACTION_SEEK" -> {
                val progress = intent.getFloatExtra("SEEK_POSITION", -1f)
                if (progress >= 0) {
                    isSeeking = true
                    MediaPlayerController.seekTo(progress)
                    lastProgress = progress
                    updatePlaybackState()
                    updateNotification(true)
                    isSeeking = false
                }
            }
            "ACTION_CLOSE" -> {
                stopProgressUpdates()
                MediaPlayerController.clearCurrentSong()
                mediaSession.isActive = false
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                updateNotification(false)
            }
        }

        return START_STICKY
    }
    
    private fun updateMediaMetadata(song: Song) {
        val metadataBuilder = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, MediaPlayerController.totalDuration.value.toLong())
            
            if (!song.imageUri.startsWith("http://") && !song.imageUri.startsWith("https://")) {
                try {
                    val artwork = loadBitmapFromUri(this@MusicService, song.imageUri)
                    if (artwork != null) {
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                    }
                } catch (e: Exception) {
                    Log.e("MusicService", "Error loading artwork: ${e.message}")
                }
            }
        }
        
        mediaSession.setMetadata(metadataBuilder.build())
        
        if (song.imageUri.startsWith("http://") || song.imageUri.startsWith("https://")) {
            serviceScope.launch {
                try {
                    val artwork = loadBitmapFromUrl(this@MusicService, song.imageUri)
                    if (artwork != null) {
                        val updatedMetadataBuilder = MediaMetadataCompat.Builder(mediaSession.controller.metadata)
                        updatedMetadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                        mediaSession.setMetadata(updatedMetadataBuilder.build())
                    }
                } catch (e: Exception) {
                    Log.e("MusicService", "Error loading remote artwork: ${e.message}")
                }
            }
        }
    }
    
    private fun updatePlaybackState() {
        val isPlaying = MediaPlayerController.isPlaying.value
        val currentPosition = MediaPlayerController.currentTime.value.toLong()
        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED

        val stateBuilder = PlaybackStateCompat.Builder().apply {
            setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            setState(state, currentPosition, 1.0f)
        }
        
        mediaSession.setPlaybackState(stateBuilder.build())
    }
    
    private fun startPlaybackTrackingJob() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            try {
                while (isActive) {
                    val song = MediaPlayerController.currentSong.value
                    val currentProgress = MediaPlayerController.progress.value

                    if (song != null && !isSeeking) {
                        val significantChange = abs(currentProgress - lastProgress) > 0.01f
                        lastProgress = currentProgress

                        updatePlaybackState()

                        if ((significantChange || currentProgress > 0.98f) && MediaPlayerController.isPlaying.value) {
                            updateNotification(false)
                        }
                    }
                    delay(PROGRESS_UPDATE_INTERVAL)
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error in playback tracking", e)
            }
        }
    }

    fun updateNotification(forceUpdate: Boolean = false) {
        val song = MediaPlayerController.currentSong.value
        if (song == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }

        val isPlaying = MediaPlayerController.isPlaying.value
        val songChanged = song.id != lastSongId

        if (songChanged) {
            updateMediaMetadata(song)
            lastSongId = song.id
        }

        val notification = createNotification(song, isPlaying)

        if (isPlaying) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(STOP_FOREGROUND_DETACH)

            if (forceUpdate || songChanged) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }

        if (forceUpdate) {
            Log.d("MusicService", "Force updating notification with progress: ${MediaPlayerController.progress.value}")
        }
    }

    private fun createNotification(song: Song, isPlaying: Boolean): Notification {
        val progressPercent = (MediaPlayerController.progress.value * 100).toInt().coerceIn(0, 100)
        
        val playPauseIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicService::class.java).apply { action = "ACTION_PLAY_PAUSE" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val nextIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MusicService::class.java).apply { action = "ACTION_NEXT" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val previousIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MusicService::class.java).apply { action = "ACTION_PREVIOUS" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val contentIntent = PendingIntent.getActivity(
            this,
            3,
            Intent(this, MainActivity::class.java).apply { 
                putExtra("open_now_playing", true)
                putExtra("restore_route", lastRoute)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val closeIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, MusicService::class.java).apply { action = "ACTION_CLOSE" },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (song.imageUri.startsWith("http://") || song.imageUri.startsWith("https://")) {
            val initialMediaStyle = MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
            
            val playPauseIcon = if (isPlaying) R.drawable.ic_pause_no_circle_36 else R.drawable.ic_play_no_circle_36
            val playPauseText = if (isPlaying) "Pause" else "Play"
            
            val initialBuilder = NotificationCompat.Builder(this, "music_channel")
                .setSmallIcon(R.drawable.ic_logo_3_white)
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .setColorized(true)
                .setColor(Color.BLACK)
                .setContentIntent(contentIntent)
                .setProgress(100, progressPercent, false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(initialMediaStyle)
                .addAction(R.drawable.ic_previous, "Previous", previousIntent)
                .addAction(playPauseIcon, playPauseText, playPauseIntent)
                .addAction(R.drawable.ic_next, "Next", nextIntent)
                .addAction(R.drawable.ic_close, "Close", closeIntent)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(closeIntent)
            
            if (isPlaying) {
                initialBuilder.setOngoing(true)
                    .setAutoCancel(false)
            } else {
                initialBuilder.setOngoing(false)
                    .setAutoCancel(true)
            }
            
            val initialNotification = initialBuilder.build()
            
            if (isPlaying) {
                initialNotification.flags = initialNotification.flags or Notification.FLAG_ONGOING_EVENT
            }
            
            serviceScope.launch {
                try {
                    val bitmap = loadBitmapFromUrl(this@MusicService, song.imageUri)
                    if (bitmap != null) {
                        val color = extractDominantColor(bitmap)
                        val colorInt = Color.rgb(
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        )
                        
                        val updatedMediaStyle = MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2)
                        
                        val updatedBuilder = NotificationCompat.Builder(this@MusicService, "music_channel")
                            .setSmallIcon(R.drawable.ic_logo_3_white)
                            .setLargeIcon(bitmap)
                            .setContentTitle(song.title)
                            .setContentText(song.artist)
                            .setColorized(true)
                            .setColor(colorInt)
                            .setContentIntent(contentIntent)
                            .setProgress(100, progressPercent, false)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setStyle(updatedMediaStyle)
                            .addAction(R.drawable.ic_previous, "Previous", previousIntent)
                            .addAction(playPauseIcon, playPauseText, playPauseIntent)
                            .addAction(R.drawable.ic_next, "Next", nextIntent)
                            .addAction(R.drawable.ic_close, "Close", closeIntent)
                            .setShowWhen(false)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDeleteIntent(closeIntent)
                        
                        if (isPlaying) {
                            updatedBuilder.setOngoing(true)
                                .setAutoCancel(false)
                        } else {
                            updatedBuilder.setOngoing(false)
                                .setAutoCancel(true)
                        }
                        
                        val updatedNotification = updatedBuilder.build()
                        
                        if (isPlaying) {
                            updatedNotification.flags = updatedNotification.flags or Notification.FLAG_ONGOING_EVENT
                        }
                        
                        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
                    }
                } catch (e: Exception) {
                    Log.e("MusicService", "Failed to load remote album art", e)
                }
            }
            
            return initialNotification
        } else {
            val albumArtBitmap = loadBitmapFromUri(this, song.imageUri)
            val dominantColorInt = albumArtBitmap?.let { 
                val color = extractDominantColor(it)
                Color.rgb(
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                )
            } ?: Color.BLACK
            
            val mediaStyle = MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
            
            val playPauseIcon = if (isPlaying) R.drawable.ic_pause_no_circle_36 else R.drawable.ic_play_no_circle_36
            val playPauseText = if (isPlaying) "Pause" else "Play"
            
            val builder = NotificationCompat.Builder(this, "music_channel")
                .setSmallIcon(R.drawable.ic_logo_3_white)
                .setLargeIcon(albumArtBitmap)
                .setContentTitle(song.title)
                .setContentText(song.artist)
                .setColorized(true)
                .setColor(dominantColorInt)
                .setContentIntent(contentIntent)
                .setProgress(100, progressPercent, false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(mediaStyle)
                .addAction(R.drawable.ic_previous, "Previous", previousIntent)
                .addAction(playPauseIcon, playPauseText, playPauseIntent)
                .addAction(R.drawable.ic_next, "Next", nextIntent)
                .addAction(R.drawable.ic_close, "Close", closeIntent)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDeleteIntent(closeIntent)
            
            if (isPlaying) {
                builder.setOngoing(true)
                    .setAutoCancel(false)
            } else {
                builder.setOngoing(false)
                    .setAutoCancel(true)
            }
                
            val notification = builder.build()
            
            if (isPlaying) {
                notification.flags = notification.flags or Notification.FLAG_ONGOING_EVENT
            }
            
            return notification
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "music_channel",
            "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Music playback controls"
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
            enableLights(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun clearNotification() {
        stopProgressUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(NOTIFICATION_ID)
        lastSongId = null
    }
    
    fun updateSongInfo(title: String, artist: String, imageUri: String) {
        lastSongId = null
        
        val metadataBuilder = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, MediaPlayerController.totalDuration.value.toLong())
            
            if (!imageUri.startsWith("http://") && !imageUri.startsWith("https://")) {
                try {
                    val artwork = loadBitmapFromUri(this@MusicService, imageUri)
                    if (artwork != null) {
                        putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                    }
                } catch (e: Exception) {
                    Log.e("MusicService", "Error loading artwork: ${e.message}")
                }
            }
        }
        
        mediaSession.setMetadata(metadataBuilder.build())
        
        if (imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
            serviceScope.launch {
                try {
                    val artwork = loadBitmapFromUrl(this@MusicService, imageUri)
                    if (artwork != null) {
                        val updatedMetadataBuilder = MediaMetadataCompat.Builder(mediaSession.controller.metadata)
                        updatedMetadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
                        mediaSession.setMetadata(updatedMetadataBuilder.build())
                    }
                } catch (e: Exception) {
                    Log.e("MusicService", "Error loading remote artwork: ${e.message}")
                }
            }
        }
        updateNotification(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        notificationManager.cancel(NOTIFICATION_ID)
        mediaSession.release()
        lastSongId = null
        instance = null
    }
}

