package com.convodoc.app.presentation.imagecompressor.model


import android.graphics.Bitmap
import android.net.Uri

data class CompressImageRequest(

    val imageUri: Uri,

    val quality: Int,
    
    val qualityLabel: String,

    val outputFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG

)