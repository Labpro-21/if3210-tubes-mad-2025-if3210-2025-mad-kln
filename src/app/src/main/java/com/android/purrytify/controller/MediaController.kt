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

    object MediaPlayerController {

        private var mediaPlayer: MediaPlayer? = null
        private var handler = Handler(Looper.getMainLooper())
        private const val UPDATE_INTERVAL = 500L

        var songList: List<Song> = emptyList()
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
            songList = songs
        }

        fun playSong(context: Context, index: Int) {
            Log.d("SONGLIST", "SONGLIST (INSIDE playSong): ${songList}")
            if (index !in songList.indices) return

            mediaPlayer?.release()

            val song = songList[index]
            currentSongIndex = index
            _currentSong.value = song
            Log.d("AudioURI", "PLAYING (INSIDE playSong): ${song.audioUri}")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context.applicationContext, Uri.parse(song.audioUri))
                prepare()
                start()
                _isPlaying.value = true
                _totalDuration.value = duration
                setOnCompletionListener {
                    playNext(context)
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
            if (currentSongIndex + 1 < songList.size) {
                playSong(context, currentSongIndex + 1)
            }
        }

        fun playPrevious(context: Context) {
            if (currentSongIndex - 1 >= 0) {
                playSong(context, currentSongIndex - 1)
            }
        }

        fun clearCurrentSong() {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            _currentSong.value = null
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