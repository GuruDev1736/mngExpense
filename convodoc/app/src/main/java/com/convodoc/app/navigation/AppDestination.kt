package com.convodoc.app.navigation

sealed class AppDestination(val route: String) {

    data object Splash : AppDestination("splash")

    data object Home : AppDestination("home")

    data object ImageCompressor :
        AppDestination("image_compressor")

    data object CompressionResult :
        AppDestination("compression_result")

    data object ImageConverter :
        AppDestination("image_converter")

    data object PdfTools :
        AppDestination("pdf_tools")

    data object Base64 :
        AppDestination("base64")

    data object QR :
        AppDestination("qr")

    data object History :
        AppDestination("history")

    data object Settings :
        AppDestination("settings")
}