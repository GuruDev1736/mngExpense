package com.convodoc.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Database

@Database(
    entities = [CompressionHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun compressionHistoryDao(): CompressionHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "convodoc_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
