package com.android.purrytify.controller

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.purrytify.data.local.entities.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class RepeatMode {
    NONE,
    REPEAT_ONE,
    REPEAT_ALL
}

object MediaPlayerController {

    private var mediaPlayer: MediaPlayer? = null
    private var handler = Handler(Looper.getMainLooper())
    private const val UPDATE_INTERVAL = 500L

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

    private var onStateChanged: (() -> Unit)? = null
    private var audioManager: AudioManager? = null
    private var deviceCallbackRegistered = false
    private val connectedDevices = mutableListOf<AudioDeviceInfo>()

    fun initialize(context: Context) {
        if (deviceCallbackRegistered) return

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                for (device in addedDevices) {
                    if (device.isValidOutputDevice()) {
                        connectedDevices.removeAll { it.id == device.id }
                        connectedDevices.add(0, device) // Most recent first
                    }
                }
                updateAndSwitch()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                for (device in removedDevices) {
                    connectedDevices.removeAll { it.id == device.id }
                }
                updateAndSwitch()
            }
        }

        audioManager?.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        deviceCallbackRegistered = true

        // Initial population
        val initial = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            ?.filter { it.isValidOutputDevice() } ?: emptyList()
        connectedDevices.clear()
        connectedDevices.addAll(initial)
        updateAndSwitch()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    _progress.value = it.currentPosition.toFloat() / it.duration
                    _currentTime.value = it.currentPosition
                    _totalDuration.value = it.duration
                    onStateChanged?.invoke()
                }
            }
            handler.postDelayed(this, UPDATE_INTERVAL)
        }
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

        _currentSong.value = song
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context.applicationContext, Uri.parse(song.audioUri))
            prepare()
            start()
            _isPlaying.value = true
            _totalDuration.value = duration
            setOnCompletionListener {
                when (_repeatMode.value) {
                    RepeatMode.REPEAT_ONE -> {// Replay current song
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
            }
            onStateChanged?.invoke()
        }
    }

    fun seekTo(position: Float) {
        mediaPlayer?.let {
            val target = (it.duration * position).toInt()
            it.seekTo(target)
            _progress.value = position
            onStateChanged?.invoke()
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
                                playSong(context, shuffledIndices[shuffledIndex])
                            } else {
                                shuffledIndex = shuffledIndices.lastIndex
                                playSong(context, shuffledIndices[shuffledIndex])
                            }
                        } else {
                            if (currentSongIndex - 1 >= 0) {
                                playSong(context, currentSongIndex - 1)
                            }
                        }
                    } else {
                        playSong(context, currentSongIndex) // Replay Current Song
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
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        _currentSong.value = null
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
        mediaPlayer?.preferredDevice = deviceInfo
    }

    private fun updateAndSwitch() {
        _availableOutputs.value = connectedDevices.toList()
        val best = connectedDevices.firstOrNull()
        mediaPlayer?.preferredDevice = best
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
        if (deviceCallbackRegistered) {
            audioManager?.unregisterAudioDeviceCallback(object : AudioDeviceCallback() {})
            deviceCallbackRegistered = false
        }
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        audioManager = null
        _isPlaying.value = false
        _progress.value = 0f
        _currentSong.value = null
        _currentTime.value = 0
        _totalDuration.value = 0
        currentSongIndex = -1
        onStateChanged?.invoke()
    }

}