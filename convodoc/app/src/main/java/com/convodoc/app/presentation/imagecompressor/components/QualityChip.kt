package com.convodoc.app.presentation.imagecompressor.components


import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun QualityChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    FilterChip(

        selected = selected,

        onClick = onClick,

        label = {

            Text(text)

        }

    )

}