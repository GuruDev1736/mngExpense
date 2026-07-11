package com.convodoc.app.presentation.imagecompressor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.convodoc.app.presentation.imagecompressor.model.EstimatedResult
import kotlin.math.min

@Composable
fun EstimatedResultCard(
    result: EstimatedResult
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Icon(
                    imageVector = Icons.Rounded.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Estimated Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

            }

            Spacer(Modifier.height(20.dp))

            ResultRow(
                title = "Original",
                value = result.originalSize
            )

            Spacer(Modifier.height(12.dp))

            ResultRow(
                title = "Estimated",
                value = result.estimatedSize
            )

            Spacer(Modifier.height(12.dp))

            ResultRow(
                title = "You'll Save",
                value = "${result.savedPercentage}%"
            )

            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = {
                    min(result.savedPercentage / 100f, 1f)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = compressionMessage(result.savedPercentage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

        }

    }

}

@Composable
private fun ResultRow(
    title: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )

    }

}

private fun compressionMessage(
    percent: Int
): String {

    return when {

        percent >= 70 -> "Excellent compression"

        percent >= 50 -> "Great compression"

        percent >= 30 -> "Good compression"

        else -> "Light compression"

    }

}