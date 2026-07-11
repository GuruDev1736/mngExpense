package com.convodoc.app.presentation.imagecompressor.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.convodoc.app.presentation.imagecompressor.model.CompressionQuality

@Composable
fun CompressionQualityCard(

    selectedQuality: CompressionQuality,

    customValue: Float,

    estimatedSize: String,

    onQualitySelected: (CompressionQuality) -> Unit,

    onSliderChange: (Float) -> Unit

) {

    ElevatedCard(

        modifier = Modifier.fillMaxWidth(),

        elevation = CardDefaults.elevatedCardElevation(2.dp)

    ) {

        Column(

            modifier = Modifier.padding(20.dp)

        ) {

            Icon(
                Icons.Rounded.Tune,
                null
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Compression Quality",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Choose the output quality",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(20.dp))

            FlowRow(

                horizontalArrangement = Arrangement.spacedBy(8.dp),

                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {

                CompressionQuality.entries.forEach {

                    QualityChip(

                        text = it.label,

                        selected = it == selectedQuality,

                        onClick = {

                            onQualitySelected(it)

                        }

                    )

                }

            }

            if (selectedQuality == CompressionQuality.CUSTOM) {

                Spacer(Modifier.height(24.dp))

                Slider(

                    value = customValue,

                    onValueChange = onSliderChange,

                    valueRange = 10f..100f

                )

                Text(

                    text = "${customValue.toInt()}%",

                    style = MaterialTheme.typography.bodyMedium

                )

            }

            Spacer(Modifier.height(20.dp))

            Text(

                text = "Estimated Output",

                style = MaterialTheme.typography.labelLarge

            )

            Text(

                text = estimatedSize,

                style = MaterialTheme.typography.headlineSmall,

                color = MaterialTheme.colorScheme.primary

            )

        }

    }

}