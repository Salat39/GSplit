package com.salat.uikit.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RatioGlyph(
    firstShare: Float,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    length: Dp = 24.dp,
    firstActive: Boolean = true,
    secondActive: Boolean = true,
    firstColor: Color = AppTheme.colors.addWindowFirstAccent,
    secondColor: Color = AppTheme.colors.addWindowSecondAccent,
    inactiveColor: Color = AppTheme.colors.contentPrimary.copy(.3f)
) {
    val thickness = length * 2 / 3
    Canvas(modifier.size(if (isLandscape) DpSize(length, thickness) else DpSize(thickness, length))) {
        val gap = (length / 12).toPx()
        val corner = CornerRadius((length / 10).toPx())
        val axisLength = if (isLandscape) size.width else size.height
        val firstLength = axisLength * firstShare - gap / 2
        val secondStart = firstLength + gap
        val stroke = Stroke((length / 18).toPx())
        fun drawPart(color: Color, active: Boolean, topLeft: Offset, partSize: Size) = if (active) {
            drawRoundRect(color, topLeft = topLeft, size = partSize, cornerRadius = corner)
        } else {
            drawInnerOutline(inactiveColor, topLeft, partSize, corner, stroke)
        }
        if (isLandscape) {
            drawPart(firstColor, firstActive, Offset.Zero, Size(firstLength, size.height))
            drawPart(secondColor, secondActive, Offset(secondStart, 0f), Size(axisLength - secondStart, size.height))
        } else {
            drawPart(firstColor, firstActive, Offset.Zero, Size(size.width, firstLength))
            drawPart(secondColor, secondActive, Offset(0f, secondStart), Size(size.width, axisLength - secondStart))
        }
    }
}

private fun DrawScope.drawInnerOutline(
    color: Color,
    topLeft: Offset,
    partSize: Size,
    corner: CornerRadius,
    stroke: Stroke
) {
    val inset = stroke.width / 2
    drawRoundRect(
        color,
        topLeft = topLeft + Offset(inset, inset),
        size = Size(partSize.width - stroke.width, partSize.height - stroke.width),
        cornerRadius = CornerRadius(corner.x - inset, corner.y - inset),
        style = stroke
    )
}
