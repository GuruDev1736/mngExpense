package com.convodoc.app.presentation.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.convodoc.app.data.database.AppDatabase
import com.convodoc.app.data.database.CompressionHistoryEntity
import com.convodoc.app.data.repository.CompressionHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CompressionHistoryRepository(
        AppDatabase.getDatabase(application).compressionHistoryDao()
    )

    private val _historyList = MutableStateFlow<List<CompressionHistoryEntity>>(emptyList())
    val historyList: StateFlow<List<CompressionHistoryEntity>> = _historyList.asStateFlow()

    private val _recentHistory = MutableStateFlow<List<CompressionHistoryEntity>>(emptyList())
    val recentHistory: StateFlow<List<CompressionHistoryEntity>> = _recentHistory.asStateFlow()

    init {
        loadAllHistory()
        loadRecentHistory()
    }

    private fun loadAllHistory() {
        viewModelScope.launch {
            repository.getAllHistory().collect { history ->
                _historyList.value = history
            }
        }
    }

    private fun loadRecentHistory(limit: Int = 5) {
        viewModelScope.launch {
            repository.getRecentHistory(limit).collect { history ->
                _recentHistory.value = history
            }
        }
    }

    fun deleteHistory(historyItem: CompressionHistoryEntity) {
        viewModelScope.launch {
            // Delete files from storage
            try {
                File(historyItem.originalFilePath).delete()
                File(historyItem.compressedFilePath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // Delete from database
            repository.deleteHistory(historyItem)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            // Delete all files
            _historyList.value.forEach { historyItem ->
                try {
                    File(historyItem.originalFilePath).delete()
                    File(historyItem.compressedFilePath).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Delete from database
            repository.deleteAllHistory()
        }
    }
}
