package com.convodoc.app.presentation.imagecompressor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.convodoc.app.presentation.imagecompressor.model.FileInfo

@Composable
fun FileInfoCard(
    info: FileInfo
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "File Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            InfoRow(
                "Name",
                info.fileName
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            InfoRow(
                "Size",
                info.fileSize
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            InfoRow(
                "Resolution",
                "${info.width} × ${info.height}"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            InfoRow(
                "Format",
                info.format
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            InfoRow(
                "Modified",
                info.modifiedDate
            )

        }

    }

}