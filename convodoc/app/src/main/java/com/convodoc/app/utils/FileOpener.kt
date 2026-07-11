package com.convodoc.app.utils


import android.content.Context
import android.content.Intent
import android.net.Uri

object FileOpener {

    fun open(
        context: Context,
        uri: Uri
    ) {

        val intent = Intent(Intent.ACTION_VIEW).apply {

            setDataAndType(uri, "image/*")

            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        }

        context.startActivity(intent)

    }

}