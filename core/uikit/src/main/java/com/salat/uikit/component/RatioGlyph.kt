package com.salat.uikit.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RatioGlyph(firstShare: Float, isLandscape: Boolean, modifier: Modifier = Modifier, length: Dp = 24.dp) {
    val firstColor = AppTheme.colors.addWindowFirstAccent
    val secondColor = AppTheme.colors.addWindowSecondAccent
    val thickness = length * 2 / 3
    Canvas(modifier.size(if (isLandscape) DpSize(length, thickness) else DpSize(thickness, length))) {
        val gap = (length / 12).toPx()
        val corner = CornerRadius((length / 10).toPx())
        val axisLength = if (isLandscape) size.width else size.height
        val firstLength = axisLength * firstShare - gap / 2
        val secondStart = firstLength + gap
        if (isLandscape) {
            drawRoundRect(firstColor, size = Size(firstLength, size.height), cornerRadius = corner)
            drawRoundRect(
                secondColor,
                topLeft = Offset(secondStart, 0f),
                size = Size(axisLength - secondStart, size.height),
                cornerRadius = corner
            )
        } else {
            drawRoundRect(firstColor, size = Size(size.width, firstLength), cornerRadius = corner)
            drawRoundRect(
                secondColor,
                topLeft = Offset(0f, secondStart),
                size = Size(size.width, axisLength - secondStart),
                cornerRadius = corner
            )
        }
    }
}
