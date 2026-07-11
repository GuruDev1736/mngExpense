package com.convodoc.app.presentation.imagecompressor.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(

    message: String,

    onDismiss: () -> Unit

) {

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("OK")

            }

        },

        title = {

            Text("Compression Failed")

        },

        text = {

            Text(message)

        }

    )

}