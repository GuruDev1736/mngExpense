package com.convodoc.app.utils


import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.Formatter.formatFileSize
import com.convodoc.app.presentation.imagecompressor.model.FileInfo

object FileInfoReader {

    fun read(
        context: Context,
        uri: Uri
    ): FileInfo {

        val resolver = context.contentResolver

        var name = ""

        var size = 0L

        resolver.query(uri, null, null, null, null)?.use { cursor ->

            val nameIndex =
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            val sizeIndex =
                cursor.getColumnIndex(OpenableColumns.SIZE)

            cursor.moveToFirst()

            name = cursor.getString(nameIndex)

            size = cursor.getLong(sizeIndex)

        }

        val options = BitmapFactory.Options()

        options.inJustDecodeBounds = true

        resolver.openInputStream(uri)?.use {

            BitmapFactory.decodeStream(it, null, options)

        }

        val format =
            name.substringAfterLast('.', "")

        return FileInfo(

            fileName = name,

            fileSize = formatFileSize(context, size),
            fileSizeBytes = size,

            width = options.outWidth,

            height = options.outHeight,

            format = format.uppercase(),

            modifiedDate = ""

        )

    }

}