package com.salat.uikit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

private const val ADD_ICON_ALPHA = .85f

// Shows the regular content when it fits the pane height. Otherwise it shows the compact row
@Composable
fun AdaptivePaneContent(regular: @Composable () -> Unit, compact: @Composable () -> Unit) = Layout(
    contents = listOf(regular, compact)
) { (regularMeasurables, compactMeasurables), constraints ->
    val contentConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity)
    val regularPlaceables = regularMeasurables.map { it.measure(contentConstraints) }
    val regularHeight = regularPlaceables.maxOfOrNull { it.height } ?: 0
    val placeables = if (regularHeight <= constraints.maxHeight) {
        regularPlaceables
    } else {
        compactMeasurables.map { it.measure(contentConstraints) }
    }
    val contentWidth = placeables.maxOfOrNull { it.width } ?: 0
    val contentHeight = placeables.maxOfOrNull { it.height } ?: 0
    val width = contentWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
    val height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
    layout(width, height) {
        placeables.forEach { it.place((width - it.width) / 2, (height - it.height) / 2) }
    }
}

@Composable
fun EmptyPaneContent(
    isCompact: Boolean,
    title: String,
    subtitle: String,
    badge: @Composable (Dp) -> Unit = { DashedAddCircle(size = it) }
) {
    if (isCompact) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            badge(56.dp)
            EmptyPaneTexts(title, subtitle, alignment = Alignment.Start, textAlign = TextAlign.Start)
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            badge(76.dp)
            Spacer(Modifier.height(14.dp))
            EmptyPaneTexts(title, subtitle, alignment = Alignment.CenterHorizontally, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EmptyPaneTexts(title: String, subtitle: String, alignment: Alignment.Horizontal, textAlign: TextAlign) =
    Column(
        horizontalAlignment = alignment
    ) {
        Text(
            text = title,
            style = AppTheme.typography.buttonTitle,
            color = AppTheme.colors.contentPrimary,
            textAlign = textAlign
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = AppTheme.typography.aboutText,
            color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
            textAlign = textAlign
        )
    }

@Composable
private fun DashedAddCircle(size: Dp) {
    val strokeColor = AppTheme.colors.contentPrimary.copy(.4f)
    Box(
        modifier = Modifier
            .size(size)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawCircle(
                    color = strokeColor,
                    radius = (this.size.minDimension - strokeWidth) / 2,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary.copy(ADD_ICON_ALPHA),
            modifier = Modifier.size(size * .42f)
        )
    }
}
