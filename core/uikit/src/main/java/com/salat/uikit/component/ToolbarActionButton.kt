package com.salat.uikit.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.salat.ui.scaledWithLayout
import com.salat.uikit.theme.AppTheme
import kotlin.math.roundToInt

private val ButtonHeight = 40.dp
private val IconSize = 20.dp
private val IconTextGap = 6.dp
private val StartPadding = 12.dp
private val EndPadding = 16.dp
private const val TONAL_BACKGROUND_ALPHA = .1f
private const val DISABLED_BACKGROUND_ALPHA = .06f

// Most fonts have a cap height close to 0.7 of the font size
private const val CAP_HEIGHT_RATIO = .7f

enum class ToolbarActionStyle { Accent, Tonal }

@Composable
fun ToolbarActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ToolbarActionStyle = ToolbarActionStyle.Accent,
    enabled: Boolean = true
) {
    val colors = AppTheme.colors
    val background by animateColorAsState(
        when {
            !enabled -> colors.contentPrimary.copy(DISABLED_BACKGROUND_ALPHA)
            style == ToolbarActionStyle.Accent -> colors.contentAccent
            else -> colors.contentPrimary.copy(TONAL_BACKGROUND_ALPHA)
        }
    )
    val content by animateColorAsState(
        when {
            !enabled -> colors.contentPrimary.copy(SettingsDefaults.DISABLED_ALPHA)
            style == ToolbarActionStyle.Accent -> Color.White
            else -> colors.contentPrimary
        }
    )

    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(ButtonHeight)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(start = StartPadding, end = EndPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IconTextGap)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(IconSize)
        )
        BaselineCenteredText(
            text = text,
            color = content,
            style = AppTheme.typography.settingsTitle.scaledWithLayout()
        )
    }
}

// The line box of a system font can have a large top space. The baseline keeps the letters in the center
@Composable
private fun BaselineCenteredText(text: String, color: Color, style: TextStyle) = Text(
    text = text,
    color = color,
    style = style,
    maxLines = 1,
    modifier = Modifier
        .fillMaxHeight()
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minHeight = 0))
            val height = constraints.maxHeight
            val baseline = placeable[FirstBaseline]
            val top = if (baseline == AlignmentLine.Unspecified) {
                (height - placeable.height) / 2
            } else {
                val capHeight = style.fontSize.toPx() * CAP_HEIGHT_RATIO
                ((height + capHeight) / 2 - baseline).roundToInt()
            }
            layout(placeable.width, height) { placeable.placeRelative(0, top) }
        }
)
