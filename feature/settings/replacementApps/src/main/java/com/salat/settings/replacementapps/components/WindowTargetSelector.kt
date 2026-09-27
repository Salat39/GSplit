package com.salat.settings.replacementapps.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.uikit.component.DialogInsetShape
import com.salat.uikit.component.RatioGlyph
import com.salat.uikit.theme.AppTheme

private val WindowTargets = listOf(true to false, true to true, false to true)
private val SegmentCorner = 12.dp
private val SegmentShape = RoundedCornerShape(SegmentCorner)

@Composable
internal fun WindowTargetSelector(
    first: Boolean,
    second: Boolean,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
    onChange: (first: Boolean, second: Boolean) -> Unit
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    Text(
        text = stringResource(R.string.show_in_menu),
        modifier = Modifier.padding(horizontal = 6.dp),
        style = AppTheme.typography.radioTitle,
        color = AppTheme.colors.contentPrimary.copy(.5f)
    )

    val titles = listOf(
        stringResource(if (isLandscape) R.string.left_window_short else R.string.top_window_short),
        stringResource(R.string.both_windows),
        stringResource(if (isLandscape) R.string.right_window_short else R.string.bottom_window_short)
    )
    val selectedIndex = WindowTargets.indexOf(first to second)
    val indicatorPosition by animateFloatAsState(selectedIndex.toFloat(), label = "indicatorPosition")
    val indicatorColor = AppTheme.colors.contentAccent.copy(.18f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DialogInsetShape)
            .background(AppTheme.colors.surfaceLayer1)
            .padding(4.dp)
            .drawBehind {
                val segmentWidth = size.width / WindowTargets.size
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(segmentWidth * indicatorPosition, 0f),
                    size = Size(segmentWidth, size.height),
                    cornerRadius = CornerRadius(SegmentCorner.toPx())
                )
            }
            .selectableGroup()
    ) {
        WindowTargets.forEachIndexed { index, (windowFirst, windowSecond) ->
            val isSelected = index == selectedIndex
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(SegmentShape)
                    .selectable(selected = isSelected, role = Role.RadioButton) { onChange(windowFirst, windowSecond) }
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatioGlyph(
                    firstShare = .5f,
                    isLandscape = isLandscape,
                    modifier = Modifier.alpha(if (isSelected) 1f else .55f),
                    firstActive = windowFirst,
                    secondActive = windowSecond
                )
                Text(
                    text = titles[index],
                    modifier = Modifier.weight(1f, fill = false),
                    color = if (isSelected) AppTheme.colors.settingsTitleAccent else AppTheme.colors.contentPrimary,
                    style = AppTheme.typography.dialogListTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
