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

        // Calculate based on both dimension scaling and JPEG quality
        // Lower quality = more dimension scaling + lower JPEG quality
        val actualQuality = when (quality) {
            CompressionQuality.LOW -> 40f
            CompressionQuality.MEDIUM -> 70f
            CompressionQuality.HIGH -> 90f
            CompressionQuality.CUSTOM -> customQuality
        }

        // Estimate based on quality level
        // This accounts for both dimension scaling and JPEG compression
        val compressionRatio = when {
            actualQuality <= 40 -> {
                // Low quality: 50% scaling = 25% pixel count, plus JPEG compression
                0.15f  // ~15% of original size
            }
            actualQuality <= 70 -> {
                // Medium quality: 70% scaling = 49% pixel count, plus JPEG compression
                0.35f  // ~35% of original size
            }
            else -> {
                // High quality: no scaling, just JPEG compression
                0.75f  // ~75% of original size
            }
        }

        val estimatedBytes =
            (originalBytes * compressionRatio).toLong()

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