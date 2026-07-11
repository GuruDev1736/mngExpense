package com.convodoc.app.presentation.imagecompressor.model

import android.net.Uri

data class CompressionUiState(

    val imageUri: Uri? = null,

    val fileInfo: FileInfo? = null,

    val estimatedResult: EstimatedResult? = null,

    val selectedQuality: CompressionQuality = CompressionQuality.MEDIUM,

    val customQuality: Float = 75f,

    val isCompressing: Boolean = false,

    val compressionResult: CompressImageResult? = null,

    val error: String? = null

)