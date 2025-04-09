package com.android.purrytify.data.local.repositories

import android.util.Log
import androidx.annotation.Nullable
import com.android.purrytify.data.local.dao.UserDao
import com.android.purrytify.data.local.entities.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(private val userDao: UserDao) {

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun insertUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            val now = getCurrentDate()
            val finalUser = user.copy(
                createdAt = user.createdAt ?: now,
                updatedAt = now
            )
            userDao.insertUser(finalUser)
            Log.d("Database", "Inserted/Updated User: $finalUser")
        }
    }


    fun updateUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            val now = getCurrentDate()
            val finalUser = user.copy(
                updatedAt = now
            )
            userDao.updateUser(finalUser)
            Log.d("Database", "Updated User: $finalUser")
        }
    }

    suspend fun getUserById(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserById(userId)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAllUsers()
        }
    }

    fun deleteUserById(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.deleteUserById(userId)
        }
    }

    fun deleteAllUsers() {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.deleteAllUsers()
        }
    }

}
