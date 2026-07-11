package com.convodoc.app.core.image

object FileNameGenerator {

    fun compressedName(
        originalName: String
    ): String {

        val dotIndex = originalName.lastIndexOf('.')

        if (dotIndex == -1) {
            return "${originalName}_compressed.jpg"
        }

        val name = originalName.substring(0, dotIndex)

        return "${name}_compressed.jpg"
    }
}