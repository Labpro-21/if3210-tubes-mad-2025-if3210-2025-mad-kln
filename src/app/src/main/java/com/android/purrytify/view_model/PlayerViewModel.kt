package com.android.purrytify.view_model

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.controller.MediaPlayerController
import com.android.purrytify.controller.RepeatMode
import kotlinx.coroutines.flow.StateFlow


class PlayerViewModel : ViewModel() {

    private val controller = MediaPlayerController

    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val progress: StateFlow<Float> = controller.progress
    val currentTime: StateFlow<Int> = controller.currentTime
    val totalDuration: StateFlow<Int> = controller.totalDuration
    val repeatMode: StateFlow<RepeatMode> = MediaPlayerController.repeatMode
    val isShuffled: StateFlow<Boolean> = controller.isShuffleEnabled

    fun updateSongInList(song: Song) {
        controller.updateSongInList(song)
    }

    fun isLast(): Boolean{
        return controller.isLastSong()
    }

    fun setSongs(songs: List<Song>) {
        for (song in songs) {
            Log.d("PlayerViewModel", "setSongs: ${song}")
        }
        controller.setSongs(songs)
    }

    fun updateCurrentSongInfo(title: String, artist: String, imageUri: String) {
        controller.updateCurrentSong(title, artist, imageUri)
    }

    fun playSong(context: Context, index: Int) {
        controller.playSong(context, index)
    }

    fun togglePlayPause() {
        controller.playPause()
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


    override fun onCleared() {
        super.onCleared()
        controller.release()
    }

//    fun deleteSongFromList(song: Song) {
//        controller.deleteSongFromList(song)
//    }
//
//    fun insertSongToList(song: Song) {
//        controller.insertSongToList(song)
//    }
}

@Composable
fun getPlayerViewModel(): PlayerViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(activity)
}