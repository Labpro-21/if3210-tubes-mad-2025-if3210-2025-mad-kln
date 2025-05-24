package com.android.purrytify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.purrytify.data.local.dao.PlaybackLogDao
import com.android.purrytify.data.local.dao.SongDao
import com.android.purrytify.data.local.dao.UserDao
import com.android.purrytify.data.local.entities.PlaybackLog
import com.android.purrytify.data.local.entities.Song
import com.android.purrytify.data.local.entities.User

@Database(entities = [Song::class, User::class, PlaybackLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao
    abstract fun userDao(): UserDao
    abstract fun playbackLogDao(): PlaybackLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "purrytify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
