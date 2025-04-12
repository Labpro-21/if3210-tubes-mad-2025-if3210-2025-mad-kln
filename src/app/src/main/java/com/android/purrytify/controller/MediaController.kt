    package com.android.purrytify.controller

    import android.content.Context
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

        var currentSongIndex = -1

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

        private var onStateChanged: (() -> Unit)? = null

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

            val song = _songList.value[index]
            currentSongIndex = index
            _currentSong.value = song
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context.applicationContext, Uri.parse(song.audioUri))
                prepare()
                start()
                _isPlaying.value = true
                _totalDuration.value = duration
                setOnCompletionListener {
                    when (_repeatMode.value) {
                        RepeatMode.REPEAT_ONE -> {
                            playSong(context, currentSongIndex) // Replay current song
                        }
                        RepeatMode.REPEAT_ALL -> {
                            if (currentSongIndex + 1 >= _songList.value.size) {
                                playSong(context, 0) // Loop to first song in list
                            } else {
                                playNext(context)
                            }
                        }
                        RepeatMode.NONE -> {
                            if (currentSongIndex + 1 < _songList.value.size) {
                                playNext(context)
                            } else {
                                _isPlaying.value = false
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
                    if (currentSongIndex + 1 < _songList.value.size) {
                        playSong(context, currentSongIndex + 1)
                    } else {
                        playSong(context, 0)
                    }
                }

                RepeatMode.REPEAT_ALL -> {
                    if (currentSongIndex + 1 < _songList.value.size) {
                        playSong(context, currentSongIndex + 1)
                    } else {
                        playSong(context, 0)
                    }
                }

                RepeatMode.NONE -> {
                    if (currentSongIndex + 1 < _songList.value.size) {
                        playSong(context, currentSongIndex + 1)
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
                            if (currentSongIndex - 1 >= 0) {
                                playSong(context, currentSongIndex - 1)
                            }
                        } else {
                            playSong(context, currentSongIndex) // Replay Current Song
                        }
                    }
                }

                RepeatMode.REPEAT_ALL -> {
                    if (currentSongIndex - 1 >= 0) {
                        playSong(context, currentSongIndex - 1)
                    } else {
                        playSong(context, _songList.value.lastIndex)
                    }
                }

                RepeatMode.NONE -> {
                    if (currentSongIndex - 1 >= 0) {
                        playSong(context, currentSongIndex - 1)
                    } else {
                        playSong(context, 0)
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
            return currentSongIndex == _songList.value.lastIndex
        }

        fun release() {
            handler.removeCallbacks(updateRunnable)
            mediaPlayer?.release()
            mediaPlayer = null
            _isPlaying.value = false
            _progress.value = 0f
            _currentSong.value = null
            _currentTime.value = 0
            _totalDuration.value = 0
            currentSongIndex = -1
            onStateChanged?.invoke()
        }

    }