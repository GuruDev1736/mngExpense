package com.convodoc.app.Model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ToolItem(
    val id : Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)