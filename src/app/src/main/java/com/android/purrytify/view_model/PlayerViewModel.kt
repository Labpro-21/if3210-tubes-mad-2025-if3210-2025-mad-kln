package com.android.purrytify.view_model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.controller.MediaPlayerController
import kotlinx.coroutines.flow.StateFlow


class PlayerViewModel : ViewModel() {

    private val controller = MediaPlayerController

    val currentSong: StateFlow<Song?> = controller.currentSong
    val isPlaying: StateFlow<Boolean> = controller.isPlaying
    val progress: StateFlow<Float> = controller.progress
    val currentTime: StateFlow<Int> = controller.currentTime
    val totalDuration: StateFlow<Int> = controller.totalDuration
    val songList: StateFlow<List<Song>> = controller.songList

    fun updateSongInList(song: Song) {
        controller.updateSongInList(song)
    }

    fun setSongs(songs: List<Song>) {
        controller.setSongs(songs)
    }

    fun updateCurrentSongInfo(title: String, artist: String, imageUri: String) {
        MediaPlayerController.updateCurrentSong(title, artist, imageUri)
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