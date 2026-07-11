package com.convodoc.app.presentation.imagecompressor.model

enum class CompressionQuality(
    val label: String,
    val value: Float
) {

    LOW("Low", 40f),

    MEDIUM("Medium", 70f),

    HIGH("High", 90f),

    CUSTOM("Custom", 75f)

}