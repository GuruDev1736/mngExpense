package com.convodoc.app.core.image

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

object ImageRotationHelper {

    fun rotateIfRequired(
        bitmap: Bitmap,
        inputStream: InputStream
    ): Bitmap {

        val exif = ExifInterface(inputStream)

        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotation = when (orientation) {

            ExifInterface.ORIENTATION_ROTATE_90 -> 90f

            ExifInterface.ORIENTATION_ROTATE_180 -> 180f

            ExifInterface.ORIENTATION_ROTATE_270 -> 270f

            else -> 0f
        }

        if (rotation == 0f) {
            return bitmap
        }

        val matrix = Matrix().apply {
            postRotate(rotation)
        }

        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}