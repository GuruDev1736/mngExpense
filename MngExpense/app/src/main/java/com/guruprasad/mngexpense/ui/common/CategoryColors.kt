package com.guruprasad.mngexpense.ui.common

import androidx.compose.ui.graphics.Color
import com.guruprasad.mngexpense.domain.Category

/** Chart / legend colour for each category. */
val Category.color: Color
    get() = when (this) {
        Category.FOOD -> Color(0xFF2A78D6)
        Category.TRANSPORT -> Color(0xFFEB6834)
        Category.SHOPPING -> Color(0xFF1BAF7A)
        Category.BILLS -> Color(0xFFEDA100)
        Category.ENTERTAINMENT -> Color(0xFFE87BA4)
        Category.HEALTH -> Color(0xFF008300)
        Category.OTHER -> Color(0xFF8A8F98)
    }
