package com.convodoc.app.presentation.imagecompressor

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.convodoc.app.presentation.imagecompressor.components.CompressionQualityCard
import com.convodoc.app.presentation.imagecompressor.components.EstimatedResultCard
import com.convodoc.app.presentation.imagecompressor.components.FileInfoCard
import com.convodoc.app.presentation.imagecompressor.components.ImagePickerCard
import com.convodoc.app.presentation.imagecompressor.components.ImagePreviewCard
import com.convodoc.app.presentation.imagecompressor.components.PrimaryButton
import com.convodoc.app.presentation.imagecompressor.model.CompressionQuality
import com.convodoc.app.presentation.imagecompressor.model.CompressionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressorScreen(

    state: CompressionUiState,

    onBack: () -> Unit,

    onPickImage: () -> Unit,
    onCameraImage: () -> Unit,
    onFileImage: () -> Unit,

    onQualitySelected: (CompressionQuality) -> Unit,

    onCustomQualityChanged: (Float) -> Unit,

    onCompress: () -> Unit

) {

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Text("Image Compressor")

                },

                navigationIcon = {

                    IconButton(onClick = onBack) {

                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            null
                        )

                    }

                }

            )

        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            verticalArrangement = Arrangement.spacedBy(16.dp),

            contentPadding = PaddingValues(16.dp)

        ) {

            item {

                ImagePickerCard(
                    imageUri = state.imageUri,
                    onPickImage = onPickImage,
                    onCameraClick = onCameraImage,
                    onFilesClick = onFileImage
                )

            }

            if (state.imageUri != null) {

                item {

                    ImagePreviewCard(
                        imageUri = state.imageUri
                    )

                }

                state.fileInfo?.let {

                    item {

                        FileInfoCard(it)

                    }

                }

                item {

                    CompressionQualityCard(

                        selectedQuality = state.selectedQuality,

                        customValue = state.customQuality,

                        estimatedSize = state.estimatedResult?.estimatedSize ?: "--",

                        onQualitySelected = onQualitySelected,

                        onSliderChange = onCustomQualityChanged

                    )
                }

                state.estimatedResult?.let {

                    item {

                        EstimatedResultCard(it)
                        println("Estimated Result = ${state.estimatedResult}")
                        Log.d("ImageCompressorScreen", "Estimated Result = ${state.estimatedResult}")

                    }

                }

                item {

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    PrimaryButton(

                        text = if (state.isCompressing) "Compressing..."
                        else "Compress Image",

                        enabled =
                            state.imageUri != null &&
                                    !state.isCompressing,

                        onClick = onCompress

                    )

                }

            } else {
                item {

                    EmptyImageState(
                        onPickImage = onPickImage
                    )

                }
            }

            item {

                Spacer(
                    Modifier
                        .height(24.dp)
                        .navigationBarsPadding()
                )

            }

        }

    }

}

@Composable
fun EmptyImageState(
    onPickImage: () -> Unit
) {

    Column(

        modifier = Modifier.fillMaxWidth(),

        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Icon(

            imageVector = Icons.Default.Image,

            contentDescription = null,

            modifier = Modifier.size(72.dp),

            tint = MaterialTheme.colorScheme.primary

        )

        Spacer(Modifier.height(16.dp))

        Text(
            "No Image Selected"
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Choose an image to start compressing.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onPickImage
        ) {

            Text("Choose Image")

        }

    }

}