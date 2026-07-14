package com.convodoc.app.core.imageCompression

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.convodoc.app.data.database.AppDatabase
import com.convodoc.app.data.database.CompressionHistoryEntity
import com.convodoc.app.data.repository.CompressionHistoryRepository
import com.convodoc.app.presentation.imagecompressor.model.CompressImageRequest
import com.convodoc.app.presentation.imagecompressor.model.CompressImageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ImageCompressionManager(
    private val context: Context
) {
    
    private val repository = CompressionHistoryRepository(
        AppDatabase.getDatabase(context).compressionHistoryDao()
    )

    suspend fun compress(
        request: CompressImageRequest
    ): CompressImageResult = withContext(Dispatchers.IO) {

        val resolver = context.contentResolver

        val originalSize =
            resolver.openAssetFileDescriptor(
                request.imageUri,
                "r"
            )?.length ?: 0L
        
        // Copy original to private storage for history
        val originalFileName = "original_${System.currentTimeMillis()}.jpg"
        val originalFile = File(context.filesDir, "compression_history/$originalFileName")
        originalFile.parentFile?.mkdirs()
        
        resolver.openInputStream(request.imageUri)?.use { input ->
            FileOutputStream(originalFile).use { output ->
                input.copyTo(output)
            }
        }

        val originalBitmap =
            resolver.openInputStream(request.imageUri).use {

                BitmapFactory.decodeStream(it)

            } ?: throw IllegalStateException("Unable to decode image")

        // Scale bitmap based on quality to actually reduce file size
        // Lower quality = more aggressive scaling
        val scaleFactor = when (request.quality) {
            in 0..40 -> 0.5f   // Low quality: scale to 50% dimensions
            in 41..70 -> 0.7f  // Medium quality: scale to 70% dimensions
            else -> 1.0f       // High quality: keep original dimensions
        }

        val bitmap = if (scaleFactor < 1.0f) {
            val newWidth = (originalBitmap.width * scaleFactor).toInt()
            val newHeight = (originalBitmap.height * scaleFactor).toInt()
            val scaled = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            if (scaled != originalBitmap) {
                originalBitmap.recycle()
            }
            scaled
        } else {
            originalBitmap
        }

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

        // Clean up bitmap
        bitmap.recycle()

        val compressedSize =
            resolver.openAssetFileDescriptor(
                outputUri,
                "r"
            )?.length ?: 0L
        
        // Copy compressed file to private storage
        val compressedFileName = "compressed_${System.currentTimeMillis()}.jpg"
        val compressedFile = File(context.filesDir, "compression_history/$compressedFileName")
        
        resolver.openInputStream(outputUri)?.use { input ->
            FileOutputStream(compressedFile).use { output ->
                input.copyTo(output)
            }
        }

        val savedBytes =
            originalSize - compressedSize

        val percentage =
            if (originalSize == 0L) 0
            else
                ((savedBytes * 100f) / originalSize)
                    .roundToInt()
        
        // Save to history
        val historyEntity = CompressionHistoryEntity(
            timestamp = System.currentTimeMillis(),
            originalFilePath = originalFile.absolutePath,
            compressedFilePath = compressedFile.absolutePath,
            originalSize = originalSize,
            compressedSize = compressedSize,
            savedBytes = savedBytes,
            savedPercentage = percentage,
            quality = request.quality,
            qualityLabel = request.qualityLabel
        )
        
        repository.insertHistory(historyEntity)

        CompressImageResult(

            outputUri = outputUri,

            originalSize = originalSize,

            compressedSize = compressedSize,

            savedBytes = savedBytes,

            savedPercentage = percentage

        )

    }

}