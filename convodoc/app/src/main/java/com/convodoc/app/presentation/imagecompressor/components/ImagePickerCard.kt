package com.convodoc.app.presentation.imagecompressor.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImagePickerCard(
    imageUri: Uri?,
    onPickImage: () -> Unit,
    onCameraClick: () -> Unit = {},
    onFilesClick: () -> Unit = {}
) {

    OutlinedCard(

        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPickImage() },

        shape = RoundedCornerShape(24.dp),

        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {

            Icon(
                imageVector = Icons.Rounded.Collections,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (imageUri == null)
                    "Select an Image"
                else
                    "Change Image",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "JPG • PNG • WEBP • HEIC",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            HorizontalDivider()

            Spacer(Modifier.height(20.dp))

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceEvenly

            ) {

                PickerChip(
                    icon = Icons.Rounded.PhotoLibrary,
                    label = "Gallery",
                    onClick = onPickImage
                )

                PickerChip(
                    icon = Icons.Rounded.PhotoCamera,
                    label = "Camera",
                    onClick = onCameraClick
                )

                PickerChip(
                    icon = Icons.Rounded.FolderOpen,
                    label = "Files",
                    onClick = onFilesClick
                )

            }

        }

    }

}