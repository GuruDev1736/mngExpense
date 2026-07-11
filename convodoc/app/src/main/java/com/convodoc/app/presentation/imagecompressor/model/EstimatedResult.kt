package com.convodoc.app.presentation.imagecompressor.model

data class EstimatedResult(
    val estimatedBytes: Long,
    val estimatedSize: String,
    val savedPercentage: Int,
    val originalSize: String
)