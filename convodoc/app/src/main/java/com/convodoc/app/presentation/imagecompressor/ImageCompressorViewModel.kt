package com.convodoc.app.presentation.imagecompressor

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.convodoc.app.core.imageCompression.ImageCompressionManager
import com.convodoc.app.presentation.imagecompressor.model.CompressImageRequest
import com.convodoc.app.presentation.imagecompressor.model.CompressionQuality
import com.convodoc.app.presentation.imagecompressor.model.CompressionUiState
import com.convodoc.app.presentation.imagecompressor.model.EstimatedResult
import com.convodoc.app.utils.CompressionEstimator
import com.convodoc.app.utils.FileInfoReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageCompressorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CompressionUiState())
    val state: StateFlow<CompressionUiState> = _state

    private val compressionManager =
        ImageCompressionManager(application)

    fun onImageSelected(uri: Uri?) {

        if (uri == null) return

        viewModelScope.launch {

            val info = FileInfoReader.read(
                getApplication(),
                uri
            )

            val estimated = CompressionEstimator.calculateEstimatedResult(
                context = getApplication(),
                originalBytes = info.fileSizeBytes,
                quality = _state.value.selectedQuality,
                customQuality = _state.value.customQuality
            )

            _state.update {

                it.copy(
                    imageUri = uri,
                    fileInfo = info,
                    estimatedResult = estimated,

                    // Clear previous compression
                    compressionResult = null,

                    error = null,

                    isCompressing = false
                )

            }

        }

    }

    fun onQualitySelected(quality: CompressionQuality) {

        _state.update { current ->

            val updated = current.copy(
                selectedQuality = quality
            )

            updated.copy(
                estimatedResult = recalculateEstimate(updated)
            )
        }
    }


    fun onCustomQualityChanged(value: Float) {

        _state.update { current ->

            val updated = current.copy(
                customQuality = value
            )

            updated.copy(
                estimatedResult = recalculateEstimate(updated)
            )
        }
    }

    fun compressImage() {

        if (_state.value.isCompressing) return
        val uri = state.value.imageUri ?: return
        val quality = when (state.value.selectedQuality) {

            CompressionQuality.LOW -> 40

            CompressionQuality.MEDIUM -> 65

            CompressionQuality.HIGH -> 90

            CompressionQuality.CUSTOM ->
                state.value.customQuality.toInt()

        }

        viewModelScope.launch(Dispatchers.IO) {

            try {

                _state.update {

                    it.copy(
                        isCompressing = true,
                        error = null
                    )

                }

                val result =
                    compressionManager.compress(

                        CompressImageRequest(

                            imageUri = uri,

                            quality = quality

                        )

                    )

                _state.update {

                    it.copy(
                        isCompressing = false,
                        compressionResult = result,
                        error = null
                    )

                }

            } catch (e: Exception) {

                _state.update {

                    it.copy(
                        isCompressing = false,
                        compressionResult = null,
                        error = e.message
                            ?: "Something went wrong while compressing the image."
                    )

                }

            }

        }

    }


    fun reset() {

//        compressionManager.clearTempFiles()

        _state.value = CompressionUiState()

    }

    fun clearCompressionResult() {

        _state.update {

            it.copy(
                compressionResult = null
            )

        }

    }

    fun clearError() {

        _state.update {

            it.copy(
                error = null
            )

        }

    }

    fun showError(message: String) {

        _state.update {

            it.copy(error = message)

        }

    }

    private fun recalculateEstimate(
        state: CompressionUiState
    ): EstimatedResult? {

        val info = state.fileInfo ?: return null

        return CompressionEstimator.calculateEstimatedResult(
            context = getApplication(),
            originalBytes = info.fileSizeBytes,
            quality = state.selectedQuality,
            customQuality = state.customQuality
        )
    }

}