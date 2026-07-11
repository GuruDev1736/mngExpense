package com.convodoc.app.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraUtils {

    fun createImageUri(context: Context): Uri {

        val directory = File(context.cacheDir, "camera")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(
            directory,
            "IMG_${System.currentTimeMillis()}.jpg"
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

    }

}