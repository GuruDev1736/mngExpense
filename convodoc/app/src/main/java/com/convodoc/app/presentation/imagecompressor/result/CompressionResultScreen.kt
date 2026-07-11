package com.convodoc.app.presentation.imagecompressor.result

import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.convodoc.app.presentation.imagecompressor.model.CompressImageResult

@Composable
fun CompressionResultScreen(
    result: CompressImageResult,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onCompressAnother: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Compression Successful",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                ResultRow(
                    title = "Original",
                    value = Formatter.formatFileSize(
                        context,
                        result.originalSize
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResultRow(
                    title = "Compressed",
                    value = Formatter.formatFileSize(
                        context,
                        result.compressedSize
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                ResultRow(
                    title = "Saved",
                    value = "${result.savedPercentage}%"
                )

            }

        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete")
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onCompressAnother
        ) {
            Text("Compress Another")
        }

    }
}