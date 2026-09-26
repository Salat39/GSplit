package com.salat.uikit.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val extraFreeWindowColors = listOf(
    Color(0xFF6A1B9A),
    Color(0xFFC62828),
    Color(0xFFEF6C00),
    Color(0xFF00838F),
    Color(0xFF4E342E),
    Color(0xFF37474F)
)

@Composable
fun freeWindowColor(index: Int) = when (index) {
    0 -> AppTheme.colors.addSplitTop
    1 -> AppTheme.colors.addSplitBottom
    else -> extraFreeWindowColors[(index - 2) % extraFreeWindowColors.size]
}
