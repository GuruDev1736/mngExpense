
package com.convodoc.app.presentation.imagecompressor

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.convodoc.app.presentation.imagecompressor.model.CompressImageResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressionResultScreen(
    result: CompressImageResult,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onCompressAnother: () -> Unit
) {

    val context = LocalContext.current

    BackHandler {
        onCompressAnother()
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text("Compression Result")

                },

                navigationIcon = {

                    IconButton(
                        onClick = onCompressAnother
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )

                    }

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ResultRow(
                        title = "Original Size",
                        value = Formatter.formatFileSize(
                            context,
                            result.originalSize
                        )
                    )

                    ResultRow(
                        title = "Compressed Size",
                        value = Formatter.formatFileSize(
                            context,
                            result.compressedSize
                        )
                    )

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

}


@Composable
fun ResultRow(
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