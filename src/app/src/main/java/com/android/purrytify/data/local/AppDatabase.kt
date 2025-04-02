package com.android.purrytify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.purrytify.data.local.dao.SongDao
import com.android.purrytify.data.local.entities.Song
import android.content.Context
import androidx.room.Room

@Database(entities = [Song::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

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