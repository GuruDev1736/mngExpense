package com.convodoc.app.utils


import android.content.Context
import android.net.Uri

object FileDeleteUtils {

    fun delete(
        context: Context,
        uri: Uri
    ) {

        context.contentResolver.delete(
            uri,
            null,
            null
        )

    }

}