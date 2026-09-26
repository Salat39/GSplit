package com.salat.uikit.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.salat.ui.rememberAvailableScreenSize
import com.salat.uikit.theme.AppTheme
import com.salat.uikit.theme.freeWindowColor

private val mapCornerRadius = 4.dp
private val mapShape = RoundedCornerShape(mapCornerRadius)
private val mapBorderWidth = 1.2.dp

// Window bounds are fractions of the screen size
@Composable
fun FreeWindowsMap(windows: List<Rect>, maxHeight: Dp, maxWidth: Dp, modifier: Modifier = Modifier) {
    val screenSize = rememberAvailableScreenSize()
    val screenAspect = screenSize.width / screenSize.height.coerceAtLeast(1.dp)
    val width = (maxHeight * screenAspect).coerceAtMost(maxWidth)
    val colors = windows.indices.map { freeWindowColor(it) }
    val borderColor = AppTheme.colors.contentPrimary.copy(.14f)

    Canvas(
        modifier
            .size(width = width, height = width / screenAspect)
            .clip(mapShape)
            .background(Color.Black.copy(.35f))
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
        val gap = 1.dp.toPx()
        val cornerRadius = CornerRadius(2.dp.toPx())
        windows.forEachIndexed { index, window ->
            drawRoundRect(
                color = colors[index],
                topLeft = Offset(window.left * size.width + gap, window.top * size.height + gap),
                size = Size(
                    width = (window.width * size.width - gap * 2).coerceAtLeast(0f),
                    height = (window.height * size.height - gap * 2).coerceAtLeast(0f)
                ),
                cornerRadius = cornerRadius
            )
        }

        val borderWidth = mapBorderWidth.toPx()
        val halfBorder = borderWidth / 2
        // Src mode replaces the window pixels below the translucent border
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(halfBorder, halfBorder),
            size = Size(size.width - borderWidth, size.height - borderWidth),
            cornerRadius = CornerRadius(mapCornerRadius.toPx() - halfBorder),
            style = Stroke(borderWidth),
            blendMode = BlendMode.Src
        )
    }
}
