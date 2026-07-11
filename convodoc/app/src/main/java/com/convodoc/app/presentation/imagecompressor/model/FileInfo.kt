package com.convodoc.app.presentation.imagecompressor.model


data class FileInfo(

    val fileName: String,

    val fileSize: String,
    val fileSizeBytes: Long,

    val width: Int,

    val height: Int,

    val format: String,

    val modifiedDate: String

)