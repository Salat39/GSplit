package com.salat.settings.quicksplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.RatioGlyph
import com.salat.uikit.theme.AppTheme
import kotlin.math.roundToInt

// The same shares and order as the preset editor. The share is the share of the first window
private val ratioPresets = listOf(
    1 / 3f to "1x2",
    1 / 2f to "1x1",
    2 / 3f to "2x1",
    3 / 7f to "3x4",
    4 / 7f to "4x3",
    3 / 5f to "3x2"
)

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(14.dp)
private val OptionsSpacing = 6.dp

internal fun matchedRatioPreset(ratio: Float) = ratioPresets.firstOrNull { (share, _) ->
    share.toPercent() == ratio.toPercent()
}?.second

private fun Float.toPercent() = (this * 100).roundToInt()

@Composable
internal fun RatioPresetsCard(
    ratio: Float,
    isLandscape: Boolean,
    onSelect: (Float) -> Unit,
    onManualClick: () -> Unit,
    modifier: Modifier = Modifier
) = Column(
    modifier = modifier
        .width(IntrinsicSize.Max)
        .shadow(10.dp, CardShape)
        .clip(CardShape)
        .background(AppTheme.colors.surfaceBackground)
        .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    ratioPresets.chunked(3).forEach { rowPresets ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(OptionsSpacing)
        ) {
            rowPresets.forEach { (share, label) ->
                RatioPresetChip(
                    share = share,
                    label = label,
                    isSelected = share.toPercent() == ratio.toPercent(),
                    isLandscape = isLandscape,
                    onClick = { onSelect(share) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.contentPrimary.copy(.08f))
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(ChipShape)
            .background(AppTheme.colors.contentPrimary.copy(.06f))
            .clickable(onClick = onManualClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        Icon(
            painter = rememberPainterResource(R.drawable.ic_manual_ratio),
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary,
            modifier = Modifier
                .size(20.dp)
                .rotate(if (isLandscape) 90f else 0f)
        )
        Text(
            text = stringResource(R.string.manual_ratio),
            color = AppTheme.colors.contentPrimary,
            style = AppTheme.typography.radioTitle,
            maxLines = 1
        )
    }
}

@Composable
private fun RatioPresetChip(
    share: Float,
    label: String,
    isSelected: Boolean,
    isLandscape: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) = Row(
    modifier = modifier
        .heightIn(min = 48.dp)
        .clip(ChipShape)
        .background(if (isSelected) AppTheme.colors.contentAccent.copy(.22f) else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
) {
    RatioGlyph(
        firstShare = share,
        isLandscape = isLandscape,
        modifier = Modifier.alpha(if (isSelected) 1f else .55f)
    )
    Text(
        text = label,
        color = if (isSelected) AppTheme.colors.settingsTitleAccent else AppTheme.colors.contentPrimary,
        style = AppTheme.typography.dialogListTitle.copy(fontFeatureSettings = "tnum"),
        maxLines = 1
    )
}
