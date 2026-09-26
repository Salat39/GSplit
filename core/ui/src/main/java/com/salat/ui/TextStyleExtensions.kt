package com.salat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
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

// The text grows with the app UI scale as the layout does. The system font scale stays
@Composable
fun TextStyle.scaledWithLayout(): TextStyle {
    val scale = appTextScale
    return copy(fontSize = fontSize.divideIfSpecified(scale), lineHeight = lineHeight.divideIfSpecified(scale))
}

private fun TextUnit.divideIfSpecified(divider: Float) = if (isSpecified) this / divider else this
