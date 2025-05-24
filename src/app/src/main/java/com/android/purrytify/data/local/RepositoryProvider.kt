package com.android.purrytify.data.local

import com.android.purrytify.data.local.repositories.PlaybackLogRepository
import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.data.local.repositories.UserRepository

object RepositoryProvider {
    private lateinit var songRepository: SongRepository
    private lateinit var userRepository: UserRepository
    private lateinit var playbackLogRepository: PlaybackLogRepository

    fun init(songRepo: SongRepository, userRepo: UserRepository, playbackLogRepo: PlaybackLogRepository) {
        songRepository = songRepo
        userRepository = userRepo
        playbackLogRepository = playbackLogRepo
    }

    fun getSongRepository(): SongRepository {
        return songRepository
    }

    fun getUserRepository(): UserRepository {
        return userRepository
    }

    fun getPlaybackLogRepository(): PlaybackLogRepository {
        return playbackLogRepository
    }
}
