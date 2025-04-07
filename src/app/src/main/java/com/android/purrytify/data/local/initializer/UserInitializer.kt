package com.android.purrytify.data.local.initializer

import com.android.purrytify.data.local.entities.User
import com.android.purrytify.data.local.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object UserInitializer {
    suspend fun initializeUsers(userRepository: UserRepository) {
        withContext(Dispatchers.IO) {
            // Clear existing users
            userRepository.deleteAllUsers()

            // Insert mock users
            val mockUsers = createMockUsers()
            for (user in mockUsers) {
                userRepository.insertUser(user)
            }
        }
    }

    private fun createMockUsers(): List<User> {
        val now = Date().toString()

        return listOf(
            User(
                id = 1,
                username = "hoshiyomi",
                email = "hoshiyomi@holo.jp",
                profilePhoto = "suisei.png",
                location = "JP",
            ),
            User(
                id = 2,
                username = "aquacrew",
                email = "aaquacrew@holo.jp",
                profilePhoto = "aqua.png",
                location = "JP",
            ),
            User(
                id = 3,
                username = "nakirigumi",
                email = "nakirigumi@holo.jp",
                profilePhoto = "ayame.png",
                location = "JP",
            )
        )
    }
}
