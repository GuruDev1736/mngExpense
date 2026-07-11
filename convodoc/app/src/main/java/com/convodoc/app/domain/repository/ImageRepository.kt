package com.convodoc.app.domain.repository


import android.net.Uri
import android.telephony.mbms.FileInfo

interface ImageRepository {

    suspend fun getFileInfo(
        uri: Uri
    ): FileInfo

    suspend fun compressImage(
        uri: Uri,
        quality: Int
    ): Uri

    suspend fun estimateCompressedSize(
        uri: Uri,
        quality: Int
    ): Long
}