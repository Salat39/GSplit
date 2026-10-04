package com.salat.settings.add.presentation.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.theme.AppTheme

private const val PANE_BADGE_ALPHA = .24f
private const val PANE_BADGE_TEXT_ALPHA = .85f
internal val PaneLabelMatchingTextPadding = 3.dp

internal enum class WindowTypeSwitchVariant {
    PANE,
    FRAME
}

@Composable
internal fun WindowTypeSwitch(
    withCaption: Boolean,
    color: Color,
    variant: WindowTypeSwitchVariant,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    textStyle: TextStyle? = null,
    locked: Boolean = false,
    enabled: Boolean = true,
    onLockedClick: () -> Unit = {}
) {
    val captionTitle = stringResource(R.string.window_type_caption)
    val noCaptionTitle = stringResource(R.string.window_type_no_caption)
    val selectedTitle = if (withCaption) captionTitle else noCaptionTitle
    val isInteractive = enabled && !locked
    val containerModifier = when (variant) {
        WindowTypeSwitchVariant.PANE ->
            modifier
                .clip(CircleShape)
                .background(Color.Black.copy(PANE_BADGE_ALPHA))

        WindowTypeSwitchVariant.FRAME ->
            modifier
                .height(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppTheme.colors.contentPrimary.copy(.07f))
    }

    Row(
        modifier = Modifier
            .alpha(if (enabled) 1f else SettingsDefaults.DISABLED_ALPHA)
            .then(containerModifier)
            .clickable(
                onClickLabel = when {
                    !isInteractive -> null
                    withCaption -> noCaptionTitle
                    else -> captionTitle
                },
                role = Role.Button,
                onClick = { if (isInteractive) onChange(!withCaption) else onLockedClick() }
            )
            .semantics { if (!showLabel) contentDescription = selectedTitle }
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(true, false).forEach { withCaptionOption ->
            WindowTypeSegment(
                withCaptionOption = withCaptionOption,
                title = if (withCaptionOption) captionTitle else noCaptionTitle,
                selected = withCaption == withCaptionOption,
                color = color,
                variant = variant,
                showLabel = showLabel,
                textStyle = textStyle,
                locked = locked
            )
        }
    }
}

@Composable
private fun WindowTypeSegment(
    withCaptionOption: Boolean,
    title: String,
    selected: Boolean,
    color: Color,
    variant: WindowTypeSwitchVariant,
    showLabel: Boolean,
    textStyle: TextStyle?,
    locked: Boolean
) {
    val iconRes = if (withCaptionOption) R.drawable.ic_window_caption else R.drawable.ic_window_clean

    when (variant) {
        WindowTypeSwitchVariant.PANE -> PaneWindowTypeSegment(
            selected = selected,
            iconRes = iconRes,
            title = title,
            color = color,
            showLabel = showLabel,
            textStyle = textStyle ?: AppTheme.typography.radioTitle
        )

        WindowTypeSwitchVariant.FRAME -> FrameWindowTypeSegment(
            selected = selected,
            withCaptionOption = withCaptionOption,
            iconRes = iconRes,
            color = color,
            locked = locked
        )
    }
}

@Composable
private fun PaneWindowTypeSegment(
    selected: Boolean,
    iconRes: Int,
    title: String,
    color: Color,
    showLabel: Boolean,
    textStyle: TextStyle
) {
    val showSelectedLabel = showLabel && selected
    val background by animateColorAsState(if (selected) color.copy(.22f) else color.copy(alpha = 0f))
    val contentColor by animateColorAsState(
        if (selected) color else AppTheme.colors.contentPrimary.copy(PANE_BADGE_TEXT_ALPHA)
    )
    Row(
        modifier = Modifier
            .heightIn(min = 28.dp)
            .clip(CircleShape)
            .background(background)
            .animateContentSize()
            .then(if (showSelectedLabel) Modifier.padding(start = 9.dp, end = 11.dp) else Modifier.width(36.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = rememberPainterResource(iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(width = 18.dp, height = 16.dp)
        )
        if (showSelectedLabel) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = textStyle,
                color = contentColor,
                maxLines = 1,
                modifier = Modifier.centerGlyphsVertically(
                    glyphHeightAboveBaselinePx = rememberGlyphHeightAboveBaselinePx(title, textStyle),
                    verticalPadding = PaneLabelMatchingTextPadding
                )
            )
        }
    }
}

@Composable
internal fun rememberGlyphHeightAboveBaselinePx(text: String, style: TextStyle): Int {
    val textSizePx = with(LocalDensity.current) { style.fontSize.toPx() }
    return remember(text, textSizePx) {
        val bounds = Rect()
        Paint().apply { textSize = textSizePx }.getTextBounds(text, 0, text.length, bounds)
        -bounds.top
    }
}

internal fun Modifier.centerGlyphsVertically(glyphHeightAboveBaselinePx: Int, verticalPadding: Dp) =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val height = placeable.height + verticalPadding.roundToPx() * 2
        layout(placeable.width, height) {
            placeable.place(0, (height + glyphHeightAboveBaselinePx) / 2 - placeable[FirstBaseline])
        }
    }

@Composable
private fun FrameWindowTypeSegment(
    selected: Boolean,
    withCaptionOption: Boolean,
    iconRes: Int,
    color: Color,
    locked: Boolean
) {
    val unselectedColor = AppTheme.colors.contentPrimary.copy(if (locked && !withCaptionOption) .3f else .72f)
    val background by animateColorAsState(if (selected) color else color.copy(alpha = 0f))
    val tint by animateColorAsState(if (selected) Color.White else unselectedColor)
    Box(
        modifier = Modifier
            .size(width = 24.dp, height = 20.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = rememberPainterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(width = 14.dp, height = 12.dp)
        )
        if (locked && !withCaptionOption) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.surfaceBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = AppTheme.colors.contentPrimary.copy(.72f),
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}
