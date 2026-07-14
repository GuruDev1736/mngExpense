package com.convodoc.app.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressionHistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CompressionHistoryEntity): Long
    
    @Query("SELECT * FROM compression_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CompressionHistoryEntity>>
    
    @Query("SELECT * FROM compression_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<CompressionHistoryEntity>>
    
    @Query("SELECT * FROM compression_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): CompressionHistoryEntity?
    
    @Delete
    suspend fun delete(history: CompressionHistoryEntity)
    
    @Query("DELETE FROM compression_history WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM compression_history")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM compression_history")
    suspend fun getCount(): Int
}
