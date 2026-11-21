package com.guruprasad.mngexpense.ui.common

import androidx.compose.ui.graphics.Color
import com.guruprasad.mngexpense.domain.Category

/** Chart / legend colour of the category (chosen by the user). */
val Category.color: Color
    get() = Color(info.color)

/** Colours offered when creating or editing a category: distinct in both light and dark theme. */
val CategoryPalette: List<Long> = listOf(
    0xFF2A78D6, 0xFF1E88E5, 0xFF00ACC1, 0xFF00897B,
    0xFF1BAF7A, 0xFF008300, 0xFF7CB342, 0xFFC0CA33,
    0xFFEDA100, 0xFFFB8C00, 0xFFEB6834, 0xFFE53935,
    0xFFE87BA4, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1,
    0xFF6D4C41, 0xFF8A8F98, 0xFF546E7A, 0xFF3949AB
)
