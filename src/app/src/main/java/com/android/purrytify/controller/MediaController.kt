package com.android.purrytify.controller

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.purrytify.data.local.RepositoryProvider
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.service.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class RepeatMode {
    NONE,
    REPEAT_ONE,
    REPEAT_ALL
}

object MediaPlayerController {

    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    private const val UPDATE_INTERVAL = 500L
    private const val SEEK_UPDATE_INTERVAL = 16L
    private var _isSeeking = false
    val isSeeking: Boolean get() = _isSeeking

    private var currentSongIndex = -1

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _currentTime = MutableStateFlow(0)
    val currentTime: StateFlow<Int> = _currentTime

    private val _totalDuration = MutableStateFlow(0)
    val totalDuration: StateFlow<Int> = _totalDuration

    private val _songList = MutableStateFlow<List<Song>>(emptyList())
    //val songList: StateFlow<List<Song>> = _songList

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _availableOutputs = MutableStateFlow<List<AudioDeviceInfo>>(emptyList())
    val availableOutputs: StateFlow<List<AudioDeviceInfo>> = _availableOutputs

    private var shuffledIndices: List<Int> = emptyList()
    private var shuffledIndex = 0

    private var pendingPlaybackMs: Long = 0L
    private var lastBatchLogTime: Long = System.currentTimeMillis()
    private const val MIN_LOG_MS: Long = 5000L
    private const val BATCH_LOG_MS: Long = 60000L

    private val playbackLogRepository= RepositoryProvider.getPlaybackLogRepository()
    private val songRepository= RepositoryProvider.getSongRepository()
    private var onStateChanged: (() -> Unit)? = null
    private var audioManager: AudioManager? = null
    private var deviceCallbackRegistered = false
    private val connectedDevices = mutableListOf<AudioDeviceInfo>()
    private var selectedOutput: AudioDeviceInfo? = null

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying || _isPlaying.value) {
                        val newProgress = player.currentPosition.toFloat() / player.duration
                        val newCurrentTime = player.currentPosition
                        
                        if (isSeeking || abs(_progress.value - newProgress) > 0.01f) {
                            _progress.value = newProgress
                            _currentTime.value = newCurrentTime
                        }
                        
                        if (player.isPlaying) {
                            pendingPlaybackMs += UPDATE_INTERVAL
                            val now = System.currentTimeMillis()
                            if (now - lastBatchLogTime >= BATCH_LOG_MS) {
                                flushPlaybackLog(force = false)
                                lastBatchLogTime = now
                            }
                        }
                        onStateChanged?.invoke()
                    }
                } catch (e: Exception) {
                    Log.e("MediaPlayerController", "Error updating progress: ${e.message}")
                }
                handler.postDelayed(this, if (isSeeking) SEEK_UPDATE_INTERVAL else UPDATE_INTERVAL)
            } ?: return
        }
    }

    private fun flushPlaybackLog(force: Boolean) {

        if (currentSong.value == null) {
            return
        }

        val songToLog = _currentSong.value
        Log.d("MediaPlayerController", "Flushing playback log for ${songToLog?.title} with ${pendingPlaybackMs}ms")
        if (songToLog != null && (pendingPlaybackMs >= MIN_LOG_MS || (force && pendingPlaybackMs > 0))) {
            playbackLogRepository.logPlayback(
                userId = songToLog.uploaderId,
                songId = songToLog.id.toString(),
                artistId = songToLog.uploaderId.toString(),
                artistName = songToLog.artist,
                songTitle = songToLog.title,
                listenedMs = pendingPlaybackMs
            )
            Log.d("MediaPlayerController", "Logged ${pendingPlaybackMs}ms for ${songToLog.title}")
        } else if (songToLog != null && pendingPlaybackMs > 0) {
            Log.d("MediaPlayerController", "Skipped logging ${pendingPlaybackMs}ms for ${songToLog.title} (less than ${MIN_LOG_MS}ms)")
        }
        pendingPlaybackMs = 0L
    }

    private lateinit var deviceCallback: AudioDeviceCallback

    fun initialize(context: Context) {
        if (deviceCallbackRegistered) return

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        deviceCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                var shouldAutoSwitch = false
                var newHeadphoneOrBluetooth: AudioDeviceInfo? = null
                
                for (device in addedDevices) {
                    if (device.isValidOutputDevice()) {
                        connectedDevices.removeAll { it.id == device.id }
                        connectedDevices.add(0, device)
                        
                        if (device.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                            device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                            device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                            shouldAutoSwitch = true
                            newHeadphoneOrBluetooth = device
                        }
                    }
                }
                
                updateAvailableOutputs()
                
                if (shouldAutoSwitch && newHeadphoneOrBluetooth != null) {
                    setAudioOutput(newHeadphoneOrBluetooth)

                } else {
                    mediaPlayer?.preferredDevice = selectedOutput
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                for (device in removedDevices) {
                    connectedDevices.removeAll { it.id == device.id }
                }
                updateAndSwitch()
            }
        }

        audioManager?.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
        deviceCallbackRegistered = true

        val initial = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            ?.filter { it.isValidOutputDevice() } ?: emptyList()
        connectedDevices.clear()
        connectedDevices.addAll(initial)
        updateAndSwitch()
    }

    fun setSongs(songs: List<Song>) {
        _songList.value = songs
        reshuffle()
    }

    fun updateSongInList(updatedSong: Song) {
        _songList.value = _songList.value.map {
            if (it.id == updatedSong.id) updatedSong else it
        }
    }

    fun updateCurrentSong(title: String, artist: String, imageUri: String) {
        _currentSong.value = _currentSong.value?.copy(
            title = title,
            artist = artist,
            imageUri = imageUri
        )
    }

    fun playSong(context: Context, index: Int) {
        if (index !in _songList.value.indices) return
        mediaPlayer?.release()

        val song: Song
        if (_isShuffleEnabled.value) {
            val trueIndex = shuffledIndices[shuffledIndex]
            currentSongIndex = trueIndex
            song = _songList.value[trueIndex]
        } else {
            currentSongIndex = index
            song = _songList.value[currentSongIndex]
        }

        flushPlaybackLog(force = true)
        _currentSong.value = song
        CoroutineScope(Dispatchers.IO).launch {
            songRepository.updateLastPlayedDate(song)
        }
        pendingPlaybackMs = 0L
        lastBatchLogTime = System.currentTimeMillis()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(context.applicationContext, Uri.parse(song.audioUri))
            selectedOutput?.let { preferredDevice = it }
            prepare()
            start()
            _isPlaying.value = true
            _totalDuration.value = duration

            setOnCompletionListener {
                when (_repeatMode.value) {
                    RepeatMode.REPEAT_ONE -> {
                        if (_isShuffleEnabled.value) {
                            playSong(context, shuffledIndex)
                        } else {
                            playSong(context, currentSongIndex)
                        }
                    }
                    RepeatMode.REPEAT_ALL -> {
                        if (_isShuffleEnabled.value) {
                            if (shuffledIndex + 1 >= shuffledIndices.size) {
                                playSong(context, shuffledIndices[0])
                            } else {
                                playSong(context, shuffledIndices[shuffledIndex + 1])
                            }
                        } else {
                            if (currentSongIndex + 1 >= _songList.value.size) {
                                playSong(context, 0)
                            } else {
                                playNext(context)
                            }
                        }
                    }
                    RepeatMode.NONE -> {
                        if (_isShuffleEnabled.value) {
                            if (shuffledIndex + 1 < shuffledIndices.size) {
                                playSong(context, shuffledIndices[shuffledIndex + 1])
                            } else {
                                _isPlaying.value = false
                            }
                        } else {
                            if (currentSongIndex + 1 < _songList.value.size) {
                                playNext(context)
                            } else {
                                _isPlaying.value = false
                            }
                        }
                    }
                }
            }
        }
        val serviceIntent = Intent(context, MusicService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        MusicService.instance?.updateNotification()
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
        onStateChanged?.invoke()
    }

    fun playPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
                handler.removeCallbacks(updateRunnable)
                handler.post(updateRunnable)
            }
            onStateChanged?.invoke()
        }
    }

    fun seekTo(position: Float) {
        mediaPlayer?.let {
            try {
                _isSeeking = true
                val target = (it.duration * position).toInt()
                it.seekTo(target)
                _progress.value = position
                _currentTime.value = target
                
                MusicService.instance?.updateNotification(true)
                
                onStateChanged?.invoke()
                
                handler.postDelayed({
                    _isSeeking = false
                }, 32L)
            } catch (e: Exception) {
                Log.e("MediaPlayerController", "Error seeking: ${e.message}")
                _isSeeking = false
            }
        }
    }

    fun playNext(context: Context) {
        when (_repeatMode.value) {
            RepeatMode.REPEAT_ONE -> {
                _repeatMode.value = RepeatMode.REPEAT_ALL // Go next song, change mode to repeat all
            }

            RepeatMode.REPEAT_ALL -> {
                if (_isShuffleEnabled.value) {
                    shuffledIndex++
                    if (shuffledIndex >= shuffledIndices.size) {
                        shuffledIndex = 0
                    }
                    playSong(context, shuffledIndices[shuffledIndex])
                } else {
                    if (currentSongIndex + 1 < _songList.value.size) {
                        playSong(context, currentSongIndex + 1)
                    } else {
                        playSong(context, 0)
                    }
                }
            }

            RepeatMode.NONE -> {
                if (_isShuffleEnabled.value) {
                    if (shuffledIndex + 1 < shuffledIndices.size) {
                        shuffledIndex++
                        playSong(context, shuffledIndices[shuffledIndex])
                    }
                } else {
                    if (currentSongIndex + 1 < _songList.value.size) {
                        playSong(context, currentSongIndex + 1)
                    }
                }
            }
        }
    }

    fun playPrevious(context: Context) {
        when (_repeatMode.value) {
            RepeatMode.REPEAT_ONE -> {
                mediaPlayer?.let { player ->
                    if (player.currentPosition <= 5000) {
                        _repeatMode.value = RepeatMode.REPEAT_ALL
                        if (_isShuffleEnabled.value) {
                            if (shuffledIndex - 1 >= 0) {
                                shuffledIndex--
                            } else {
                                shuffledIndex = shuffledIndices.lastIndex
                            }
                            playSong(context, shuffledIndices[shuffledIndex])
                        } else {
                            if (currentSongIndex - 1 >= 0) {
                                playSong(context, currentSongIndex - 1)
                            }
                        }
                    } else {
                        seekTo(0f)
                    }
                }
            }

            RepeatMode.REPEAT_ALL -> {
                if (_isShuffleEnabled.value) {
                    shuffledIndex--
                    if (shuffledIndex < 0) {
                        shuffledIndex = shuffledIndices.lastIndex
                    }
                    playSong(context, shuffledIndices[shuffledIndex])
                } else {
                    if (currentSongIndex - 1 >= 0) {
                        playSong(context, currentSongIndex - 1)
                    } else {
                        playSong(context, _songList.value.lastIndex)
                    }
                }
            }

            RepeatMode.NONE -> {
                if (_isShuffleEnabled.value) {
                    if (shuffledIndex > 0) {
                        shuffledIndex--
                        playSong(context, shuffledIndices[shuffledIndex])
                    } else {
                        seekTo(0f)
                    }
                } else {
                    if (currentSongIndex - 1 >= 0) {
                        playSong(context, currentSongIndex - 1)
                    } else {
                        seekTo(0f)
                    }
                }
            }
        }
    }

    fun clearCurrentSong() {
        flushPlaybackLog(force = true)
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _currentSong.value = null
        pendingPlaybackMs = 0L
        _isPlaying.value = false
        _progress.value = 0f
        _currentTime.value = 0
        _totalDuration.value = 0
        onStateChanged?.invoke()
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun isLastSong(): Boolean {
        return if (_isShuffleEnabled.value) {
            shuffledIndex == shuffledIndices.lastIndex
        } else {
            currentSongIndex == _songList.value.lastIndex
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        reshuffle()
    }

    private fun reshuffle() {
        val currentIndex = currentSongIndex
        val remaining = _songList.value.indices.filter { it != currentIndex }.shuffled()
        shuffledIndices = listOf(currentIndex) + remaining
        shuffledIndex = 0
    }

    fun setAudioOutput(deviceInfo: AudioDeviceInfo) {
        if (deviceInfo != selectedOutput) {
            mediaPlayer?.preferredDevice = deviceInfo
            selectedOutput = deviceInfo
        }
    }

    private fun updateAndSwitch() {
        updateAvailableOutputs()

        if (selectedOutput == null || !connectedDevices.any { it.id == selectedOutput?.id }) {
            selectedOutput = connectedDevices.firstOrNull()
        }

        mediaPlayer?.preferredDevice = selectedOutput
    }
    
    private fun updateAvailableOutputs() {
        _availableOutputs.value = connectedDevices.toList()
    }

    private fun AudioDeviceInfo.isValidOutputDevice(): Boolean {
        return type in listOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        )
    }

    fun release() {
        handler.removeCallbacks(updateRunnable)
        flushPlaybackLog(force = true)

        if (deviceCallbackRegistered) {
            audioManager?.unregisterAudioDeviceCallback(deviceCallback)
            deviceCallbackRegistered = false
        }
        mediaPlayer?.release()
        mediaPlayer = null
        audioManager = null
        _isPlaying.value = false
        _progress.value = 0f
        _currentSong.value = null
        _currentTime.value = 0
        _totalDuration.value = 0
        currentSongIndex = -1
        pendingPlaybackMs = 0L
        onStateChanged?.invoke()
    }

}