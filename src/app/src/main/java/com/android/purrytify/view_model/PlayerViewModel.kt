package com.android.purrytify.view_model

import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.controller.MediaPlayerController
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.controller.RepeatMode
import com.android.purrytify.service.MusicService
import kotlinx.coroutines.flow.StateFlow


class PlayerViewModel : ViewModel() {

    private val controller = MediaPlayerController

    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val progress: StateFlow<Float> = controller.progress
    val currentTime: StateFlow<Int> = controller.currentTime
    val totalDuration: StateFlow<Int> = controller.totalDuration
    val repeatMode: StateFlow<RepeatMode> = controller.repeatMode
    val isShuffled: StateFlow<Boolean> = controller.isShuffleEnabled
    val availableOutputs: StateFlow<List<AudioDeviceInfo>> = controller.availableOutputs

    fun initialize(context: Context) {
        controller.initialize(context)
    }

    fun updateSongInList(song: Song) {
        controller.updateSongInList(song)
    }

    fun isLast(): Boolean{
        return controller.isLastSong()
    }

    fun setSongs(songs: List<Song>) {
        for (song in songs) {
            Log.d("PlayerViewModel", "setSongs: $song")
        }
        controller.setSongs(songs)
    }

    fun updateCurrentSongInfo(title: String, artist: String, imageUri: String) {
        controller.updateCurrentSong(title, artist, imageUri)
    }

    fun playSong(context: Context, index: Int) {
        Log.d("PlayerViewModel", "playSong: $index")
        controller.playSong(context, index)
        val serviceIntent = Intent(context, MusicService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun togglePlayPause(context: Context) {
        controller.playPause()
        val serviceIntent = Intent(context, MusicService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
        MusicService.instance?.updateNotification(true)
    }

    fun seekTo(position: Float) {
        controller.seekTo(position)
    }

    fun playNext(context: Context) {
        controller.playNext(context)
    }

    fun playPrevious(context: Context) {
        controller.playPrevious(context)
    }

    fun clearCurrent(){
        controller.clearCurrentSong()
    }

    fun toggleRepeatMode() {
        val current = MediaPlayerController.repeatMode.value
        val next = when (current) {
            RepeatMode.NONE -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.NONE
        }
        MediaPlayerController.setRepeatMode(next)
    }

    fun toggleShuffle() {
        controller.toggleShuffle()
    }

    fun setAudioOutput(deviceInfo: AudioDeviceInfo) {
        controller.setAudioOutput(deviceInfo)
    }

    fun release() {
        controller.release()
    }

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }

}

@Composable
fun getPlayerViewModel(): PlayerViewModel {
    val context = LocalContext.current as ComponentActivity
    return viewModel(context)
}
