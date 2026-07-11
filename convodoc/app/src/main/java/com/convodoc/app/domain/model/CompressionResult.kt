package com.convodoc.app.domain.model


import android.net.Uri

data class CompressionResult(

    val uri: Uri,

    val originalSize: Long,

    val compressedSize: Long,

    val percentageSaved: Int

)