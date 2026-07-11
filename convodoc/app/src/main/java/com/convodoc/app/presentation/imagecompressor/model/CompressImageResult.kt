package com.convodoc.app.presentation.imagecompressor.model

import android.net.Uri

data class CompressImageResult(

    val outputUri: Uri,

    val originalSize: Long,

    val compressedSize: Long,

    val savedBytes: Long,

    val savedPercentage: Int

)