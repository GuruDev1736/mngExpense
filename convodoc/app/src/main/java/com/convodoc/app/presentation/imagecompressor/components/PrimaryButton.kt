package com.convodoc.app.presentation.imagecompressor.components


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(

    text: String,

    enabled: Boolean = true,

    onClick: () -> Unit

) {

    Button(

        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),

        enabled = enabled,

        shape = MaterialTheme.shapes.large,

        colors = ButtonDefaults.buttonColors(),

        onClick = onClick

    ) {

        Text(text)

    }

}