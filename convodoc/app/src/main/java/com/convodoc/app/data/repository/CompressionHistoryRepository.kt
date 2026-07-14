package com.convodoc.app.data.repository

import com.convodoc.app.data.database.CompressionHistoryDao
import com.convodoc.app.data.database.CompressionHistoryEntity
import kotlinx.coroutines.flow.Flow

class CompressionHistoryRepository(
    private val dao: CompressionHistoryDao
) {
    
    fun getAllHistory(): Flow<List<CompressionHistoryEntity>> {
        return dao.getAllHistory()
    }
    
    fun getRecentHistory(limit: Int = 5): Flow<List<CompressionHistoryEntity>> {
        return dao.getRecentHistory(limit)
    }
    
    suspend fun getHistoryById(id: Long): CompressionHistoryEntity? {
        return dao.getHistoryById(id)
    }
    
    suspend fun insertHistory(history: CompressionHistoryEntity): Long {
        return dao.insert(history)
    }
    
    suspend fun deleteHistory(history: CompressionHistoryEntity) {
        dao.delete(history)
    }
    
    suspend fun deleteHistoryById(id: Long) {
        dao.deleteById(id)
    }
    
    suspend fun deleteAllHistory() {
        dao.deleteAll()
    }
    
    suspend fun getCount(): Int {
        return dao.getCount()
    }
}
