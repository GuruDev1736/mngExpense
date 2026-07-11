package com.convodoc.app.presentation.imagecompressor.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PickerChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {

    AssistChip(

        onClick = onClick,

        label = {

            Text(label)

        },

        leadingIcon = {

            Icon(
                imageVector = icon,
                contentDescription = null
            )

        }

    )

}