package com.android.purrytify.data.local

import com.android.purrytify.data.local.repositories.SongRepository
import com.android.purrytify.data.local.repositories.UserRepository

object RepositoryProvider {
    private lateinit var songRepository: SongRepository
    private lateinit var userRepository: UserRepository

    fun init(songRepo: SongRepository, userRepo: UserRepository) {
        songRepository = songRepo
        userRepository = userRepo
    }

    fun getSongRepository(): SongRepository {
        return songRepository
    }

    fun getUserRepository(): UserRepository {
        return userRepository
    }
}
