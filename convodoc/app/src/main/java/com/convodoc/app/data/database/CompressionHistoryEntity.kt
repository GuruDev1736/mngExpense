package com.convodoc.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compression_history")
data class CompressionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val timestamp: Long,
    
    val originalFilePath: String,
    
    val compressedFilePath: String,
    
    val originalSize: Long,
    
    val compressedSize: Long,
    
    val savedBytes: Long,
    
    val savedPercentage: Int,
    
    val quality: Int,
    
    val qualityLabel: String
)
