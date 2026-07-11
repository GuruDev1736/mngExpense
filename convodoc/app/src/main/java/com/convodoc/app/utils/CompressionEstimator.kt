package com.convodoc.app.utils

import android.content.Context
import android.text.format.Formatter
import com.convodoc.app.presentation.imagecompressor.model.CompressionQuality
import com.convodoc.app.presentation.imagecompressor.model.EstimatedResult

object CompressionEstimator {

    fun calculateEstimatedResult(
        context: Context,
        originalBytes: Long,
        quality: CompressionQuality,
        customQuality: Float
    ): EstimatedResult {

        val qualityPercent = when (quality) {
            CompressionQuality.LOW -> 40
            CompressionQuality.MEDIUM -> 65
            CompressionQuality.HIGH -> 90
            CompressionQuality.CUSTOM -> customQuality.toInt()
        }

        val estimatedBytes =
            (originalBytes * qualityPercent / 100f).toLong()

        val savedPercentage =
            if (originalBytes > 0) {

                (((originalBytes - estimatedBytes) * 100) / originalBytes)
                    .toInt()
                    .coerceIn(0, 100)

            } else {

                0

            }

        return EstimatedResult(
            estimatedBytes = estimatedBytes,
            estimatedSize = Formatter.formatFileSize(
                context,
                estimatedBytes
            ),
            savedPercentage = savedPercentage,
            originalSize = Formatter.formatFileSize(
                context,
                originalBytes
            )

        )
    }
}