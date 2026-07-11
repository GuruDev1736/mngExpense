package com.convodoc.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object ShareUtils {

    fun shareImage(
        context: Context,
        uri: Uri
    ) {

        val intent = Intent(Intent.ACTION_SEND).apply {

            type = "image/*"

            putExtra(Intent.EXTRA_STREAM, uri)

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        }

        context.startActivity(
            Intent.createChooser(intent, "Share Image")
        )

    }

}