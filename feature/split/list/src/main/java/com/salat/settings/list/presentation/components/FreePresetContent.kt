package com.salat.settings.list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.resources.R
import com.salat.settings.list.presentation.entity.DisplayAppPreset
import com.salat.settings.list.presentation.entity.DisplayFreeWindow
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.ui.modifyFontSize
import com.salat.ui.systemIconsAreRound
import com.salat.uikit.component.FreeWindowsMap
import com.salat.uikit.theme.AppTheme
import com.salat.uikit.theme.freeWindowColor

private const val MAP_MAX_WIDTH = 100
private const val CHIP_HEIGHT = 28
private const val CHIP_ICON_SIZE = 18
private const val CHIP_ICON_INSET = 5
private const val CHIP_SQUARE_ICON_INSET = 8
private const val CHIP_TEXT_INSET = 12
private const val CHIP_TRAILING_ICON_INSET = 10
private const val CHIP_SPACING = 8
private const val STATUS_GAP = 6
private const val CHIP_BACKGROUND_ALPHA = .3f
private const val CHIP_CONTENT_WHITE_FRACTION = .6f
private const val CHIP_ICON_GAP_EM = .3f
private const val PLAY_ICON_EM = .7f
private const val PIN_ICON_EM = .76f
private const val CAPTION_GLYPH_EM = .65f

@Composable
internal fun FreePresetContent(preset: DisplaySplitPreset, showWindowType: Boolean, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val subtitleStyle = AppTheme.typography.dialogSubtitle
    val mapHeight = remember(textMeasurer, density, subtitleStyle) {
        val statusHeight = with(density) { textMeasurer.measure("A", subtitleStyle).size.height.toDp() }
        CHIP_HEIGHT.dp + STATUS_GAP.dp + maxOf(statusHeight, PresetStatusBadgeSize)
    }
    val mapWindows = remember(preset.windows) { preset.windows.map { Rect(it.left, it.top, it.right, it.bottom) } }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FreeWindowsMap(mapWindows, maxHeight = mapHeight, maxWidth = MAP_MAX_WIDTH.dp)
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(STATUS_GAP.dp)
        ) {
            WindowChipsRow(preset.windows, showCaptions = showWindowType)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.free_mode_short) + " · " +
                        pluralStringResource(R.plurals.windows_count, preset.windows.size, preset.windows.size),
                    style = subtitleStyle,
                    color = AppTheme.colors.contentPrimary.copy(.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (preset.darkBackground) PresetStatusBadge(R.drawable.ic_moon)
                if (preset.quickAccess) PresetStatusBadge(R.drawable.ic_star)
            }
        }
    }
}

@Composable
private fun WindowChipsRow(windows: List<DisplayFreeWindow>, showCaptions: Boolean) = SubcomposeLayout { constraints ->
    val spacing = CHIP_SPACING.dp.roundToPx()
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val chips = subcompose("chips") {
        windows.forEachIndexed { index, window ->
            WindowChip(
                app = window.app,
                alwaysOnTop = window.alwaysOnTop,
                // A pinned window always opens with the top bar. The pin icon shows this
                showCaption = showCaptions && window.app.withCaption && !window.alwaysOnTop,
                color = freeWindowColor(index)
            )
        }
    }
    val chipHeight = CHIP_HEIGHT.dp.roundToPx()
    val chipEnds = chips.runningFold(-spacing) { end, chip -> end + spacing + chip.maxIntrinsicWidth(chipHeight) }
        .drop(1)

    var visibleCount = chips.size
    var moreChip: Placeable? = null
    fun moreChipSpace() = moreChip?.let { spacing + it.width } ?: 0
    while (visibleCount > 1 && chipEnds[visibleCount - 1] + moreChipSpace() > constraints.maxWidth) {
        visibleCount--
        val hiddenCount = chips.size - visibleCount
        moreChip = subcompose(visibleCount) { MoreChip(hiddenCount) }.single().measure(looseConstraints)
    }

    val chipConstraints = looseConstraints.copy(maxWidth = (constraints.maxWidth - moreChipSpace()).coerceAtLeast(0))
    val rowItems = chips.take(visibleCount).map { it.measure(chipConstraints) } + listOfNotNull(moreChip)
    val rowWidth = rowItems.sumOf { it.width } + spacing * (rowItems.size - 1).coerceAtLeast(0)
    layout(constraints.constrainWidth(rowWidth), rowItems.maxOfOrNull { it.height } ?: 0) {
        var x = 0
        rowItems.forEach {
            it.placeRelative(x, 0)
            x += it.width + spacing
        }
    }
}

@Composable
private fun MoreChip(count: Int) = Box(
    modifier = Modifier
        .height(CHIP_HEIGHT.dp)
        .widthIn(min = CHIP_HEIGHT.dp)
        .clip(CircleShape)
        .background(AppTheme.colors.contentPrimary.copy(.14f))
        .padding(horizontal = 6.dp),
    contentAlignment = Alignment.Center
) {
    Text(
        text = "+$count",
        style = AppTheme.typography.sourceType.modifyFontSize(1),
        color = AppTheme.colors.contentPrimary,
        maxLines = 1
    )
}

@Composable
private fun WindowChip(app: DisplayAppPreset, alwaysOnTop: Boolean, showCaption: Boolean, color: Color) = Row(
    modifier = Modifier
        .height(CHIP_HEIGHT.dp)
        .clip(CircleShape)
        .background(color.copy(CHIP_BACKGROUND_ALPHA))
        .padding(
            start = chipStartInset(hasIcon = app.icon != null),
            end = chipEndInset(hasIcon = app.autoPlay == true || alwaysOnTop || showCaption)
        ),
    verticalAlignment = Alignment.CenterVertically
) {
    val context = LocalContext.current
    val contentColor = lerp(color, Color.White, CHIP_CONTENT_WHITE_FRACTION)
    val titleStyle = AppTheme.typography.sourceType.modifyFontSize(1)
    app.icon?.let { icon ->
        AsyncImage(
            model = remember(icon) { ImageRequest.Builder(context).data(icon).build() },
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(CHIP_ICON_SIZE.dp)
                .clip(RoundedCornerShape((CHIP_ICON_SIZE * .22f).dp))
        )
        Spacer(Modifier.width(7.dp))
    }
    Text(
        text = app.title,
        modifier = Modifier.weight(1f, fill = false),
        style = titleStyle,
        color = contentColor,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    val iconGap = titleStyle.emToDp(CHIP_ICON_GAP_EM)
    if (app.autoPlay == true) {
        Spacer(Modifier.width(iconGap))
        TextAlignedIcon(R.drawable.ic_play, titleStyle, contentColor, widthEm = PLAY_ICON_EM, heightEm = PLAY_ICON_EM)
    }
    if (alwaysOnTop) {
        Spacer(Modifier.width(iconGap))
        TextAlignedIcon(R.drawable.ic_pin, titleStyle, contentColor, widthEm = PIN_ICON_EM, heightEm = PIN_ICON_EM)
    }
    if (showCaption) {
        Spacer(Modifier.width(iconGap))
        WindowCaptionGlyph(textStyle = titleStyle, tint = contentColor, heightEm = CAPTION_GLYPH_EM)
    }
}

private fun chipEndInset(hasIcon: Boolean) = if (hasIcon) CHIP_TRAILING_ICON_INSET.dp else CHIP_TEXT_INSET.dp

// A square icon needs more start space because its corners come close to the round chip edge
private fun chipStartInset(hasIcon: Boolean) = when {
    !hasIcon -> CHIP_TEXT_INSET.dp
    systemIconsAreRound -> CHIP_ICON_INSET.dp
    else -> CHIP_SQUARE_ICON_INSET.dp
}
