package com.salat.ui

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

fun TextStyle.modifyFontSize(shift: Int, letterSpacing: Int = 0) = if (shift == 0) {
    this
} else {
    copy(
        fontSize = (fontSize.value + shift).coerceAtLeast(1f).sp,
        lineHeight = (lineHeight.value + shift).coerceAtLeast(1f).sp,
        letterSpacing = if (letterSpacing != 0) letterSpacing.sp else this.letterSpacing
    )
}
