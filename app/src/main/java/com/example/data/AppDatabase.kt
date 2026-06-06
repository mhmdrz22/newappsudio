package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ResistanceTask::class, TransmissionLog::class, MarioHighScore::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resistanceDao(): ResistanceDao
    abstract fun marioDao(): MarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "resistance_secure_datastore.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
