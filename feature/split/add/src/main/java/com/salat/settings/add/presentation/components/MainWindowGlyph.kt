package com.salat.settings.add.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private const val VIEWPORT_HEIGHT = 16f
private const val PORTRAIT_VIEWPORT_WIDTH = 30f
private const val LANDSCAPE_VIEWPORT_WIDTH = 34f
private const val STROKE_WIDTH = 1.3f
private val GlyphHeight = 13.dp

private data class GlyphRect(val x: Float, val y: Float, val width: Float, val height: Float, val radius: Float)

private val portraitPanes = GlyphRect(1f, 1f, 9f, 6.4f, 1.4f) to GlyphRect(1f, 8.6f, 9f, 6.4f, 1.4f)
private val portraitFullscreen = GlyphRect(21.8f, 1f, 7.2f, 14f, 1.6f)
private val landscapePanes = GlyphRect(1f, 2f, 6f, 12f, 1.4f) to GlyphRect(8.2f, 2f, 6f, 12f, 1.4f)
private val landscapeFullscreen = GlyphRect(25.2f, 2f, 7.8f, 12f, 1.6f)

@Composable
internal fun MainWindowGlyph(isLandscape: Boolean, isFirst: Boolean, color: Color, accent: Color) {
    val viewportWidth = if (isLandscape) LANDSCAPE_VIEWPORT_WIDTH else PORTRAIT_VIEWPORT_WIDTH
    Canvas(Modifier.size(width = GlyphHeight * (viewportWidth / VIEWPORT_HEIGHT), height = GlyphHeight)) {
        val scale = size.height / VIEWPORT_HEIGHT
        val stroke = Stroke(width = STROKE_WIDTH * scale)
        val (firstPane, secondPane) = if (isLandscape) landscapePanes else portraitPanes
        val arrowStart = if (isLandscape) 17f else 13f

        drawGlyphRect(firstPane, scale, color, stroke, fill = if (isFirst) accent else null)
        drawGlyphRect(secondPane, scale, color, stroke, fill = if (isFirst) null else accent)
        drawPath(
            path = Path().apply {
                moveTo(arrowStart * scale, 8f * scale)
                lineTo((arrowStart + 6f) * scale, 8f * scale)
                moveTo((arrowStart + 3.7f) * scale, 5.7f * scale)
                lineTo((arrowStart + 6f) * scale, 8f * scale)
                lineTo((arrowStart + 3.7f) * scale, 10.3f * scale)
            },
            color = color,
            style = stroke
        )
        drawGlyphRect(if (isLandscape) landscapeFullscreen else portraitFullscreen, scale, color, stroke, accent)
    }
}

private fun DrawScope.drawGlyphRect(rect: GlyphRect, scale: Float, color: Color, stroke: Stroke, fill: Color?) {
    val topLeft = Offset(rect.x * scale, rect.y * scale)
    val size = Size(rect.width * scale, rect.height * scale)
    val cornerRadius = CornerRadius(rect.radius * scale)
    fill?.let { drawRoundRect(color = it, topLeft = topLeft, size = size, cornerRadius = cornerRadius) }
    drawRoundRect(color = fill ?: color, topLeft = topLeft, size = size, cornerRadius = cornerRadius, style = stroke)
}
