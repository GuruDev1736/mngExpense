package com.convodoc.app.core.image

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.convodoc.app.presentation.imagecompressor.model.CompressImageRequest
import com.convodoc.app.presentation.imagecompressor.model.CompressImageResult
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt

class ImageCompressionManager(
    private val context: Context
) {

    fun compress(
        request: CompressImageRequest
    ): CompressImageResult {

        val resolver = context.contentResolver

        val originalSize =
            resolver.openAssetFileDescriptor(
                request.imageUri,
                "r"
            )?.length ?: 0L

        val bitmap =
            resolver.openInputStream(request.imageUri).use {

                BitmapFactory.decodeStream(it)

            } ?: throw IllegalStateException("Unable to decode image")

        val fileName =
            "IMG_${System.currentTimeMillis()}.jpg"

        val outputUri: Uri

        val outputStream: OutputStream

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues().apply {

                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    fileName
                )

                put(
                    MediaStore.Images.Media.MIME_TYPE,
                    "image/jpeg"
                )

                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/ConvoDoc"
                )

            }

            outputUri =
                resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                ) ?: throw IllegalStateException("Cannot create output file")

            outputStream =
                resolver.openOutputStream(outputUri)
                    ?: throw IllegalStateException("Cannot open output stream")

        } else {

            val folder =
                File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ),
                    "ConvoDoc"
                )

            if (!folder.exists()) {
                folder.mkdirs()
            }

            val file =
                File(folder, fileName)

            outputUri = Uri.fromFile(file)

            outputStream =
                FileOutputStream(file)

        }

        outputStream.use {

            bitmap.compress(

                request.outputFormat,

                request.quality,

                it

            )

        }

        val compressedSize =
            resolver.openAssetFileDescriptor(
                outputUri,
                "r"
            )?.length ?: 0L

        val savedBytes =
            originalSize - compressedSize

        val percentage =
            if (originalSize == 0L) 0
            else
                ((savedBytes * 100f) / originalSize)
                    .roundToInt()

        return CompressImageResult(

            outputUri = outputUri,

            originalSize = originalSize,

            compressedSize = compressedSize,

            savedBytes = savedBytes,

            savedPercentage = percentage

        )

    }

}