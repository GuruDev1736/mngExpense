
package com.convodoc.app.presentation.imagecompressor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.convodoc.app.presentation.imagecompressor.components.ErrorDialog
import com.convodoc.app.utils.CameraUtils
import com.convodoc.app.utils.FileDeleteUtils
import com.convodoc.app.utils.FileOpener
import com.convodoc.app.utils.ShareUtils

@Composable
fun ImageCompressorRoute(
    viewModel: ImageCompressorViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var cameraUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val picker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            uri?.let {
                viewModel.onImageSelected(it)
            }

        }

    val fileLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            uri?.let {

                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                viewModel.onImageSelected(it)

            }

        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            if (success) {

                cameraUri?.let {
                    viewModel.onImageSelected(it)
                }

            } else {

                cameraUri = null

            }

        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                cameraUri = CameraUtils.createImageUri(context)

                cameraUri?.let {
                    cameraLauncher.launch(it)
                }

            } else {

                viewModel.showError(
                    "Camera permission is required to capture an image."
                )

            }

        }

    state.error?.let {

        ErrorDialog(

            message = it,

            onDismiss = {

                viewModel.clearError()

            }

        )

    }

    if (state.isCompressing) {

        Dialog(
            onDismissRequest = {}
        ) {

            Card {

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                    CircularProgressIndicator()

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Text(
                        text = "Compressing Image..."
                    )

                }

            }

        }

    }

    if (state.compressionResult == null) {

        ImageCompressorScreen(

            state = state,

            onBack = {

                viewModel.reset()
                onBack()

            },

            onPickImage = {

                picker.launch(

                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )

                )

            },

            onCameraImage = {

                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    cameraUri = CameraUtils.createImageUri(context)

                    cameraUri?.let {
                        cameraLauncher.launch(it)
                    }

                } else {

                    cameraPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )

                }

            },

            onFileImage = {

                fileLauncher.launch(
                    arrayOf("image/*")
                )

            },

            onQualitySelected = viewModel::onQualitySelected,

            onCustomQualityChanged = viewModel::onCustomQualityChanged,

            onCompress = {

                viewModel.compressImage()

            }

        )

    } else {

        BackHandler {
            viewModel.reset()
            onBack()
        }

        CompressionResultScreen(

            result = state.compressionResult!!,

            onShare = {

                ShareUtils.shareImage(
                    context,
                    state.compressionResult!!.outputUri
                )

            },

            onOpen = {

                FileOpener.open(
                    context,
                    state.compressionResult!!.outputUri
                )

            },

            onDelete = {

                FileDeleteUtils.delete(
                    context,
                    state.compressionResult!!.outputUri
                )

                viewModel.reset()

            },

            onCompressAnother = {

                viewModel.clearCompressionResult()

            }

        )

    }

}
