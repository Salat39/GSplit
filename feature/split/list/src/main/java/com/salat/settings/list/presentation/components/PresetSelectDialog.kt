package com.salat.settings.list.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.entity.RenderListType
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.BottomShadow
import com.salat.uikit.component.TopShadow
import com.salat.uikit.theme.AppTheme

private const val DIALOG_SHADOW_ELEVATION = 16
private const val DIALOG_DIM_AMOUNT = .6f

@Composable
fun PresetSelectDialog(
    presets: List<DisplaySplitPreset>,
    uiScale: Float,
    lastLaunchedHint: String,
    onSelectPreset: (DisplaySplitPreset) -> Unit,
    onSelectLastLaunched: () -> Unit,
    onDismiss: () -> Unit,
    onSelectPresetPanel: (() -> Unit)? = null,
    onSelectQuickSplit: (() -> Unit)? = null,
    noCaptionWindows: Boolean = false,
    mainWindowAvailable: Boolean = false,
    nativeSplit: Boolean = false
) = BaseDialog(
    uiScaleState = uiScale,
    onDismiss = onDismiss,
    shadowElevation = DIALOG_SHADOW_ELEVATION.dp,
    dimAmount = DIALOG_DIM_AMOUNT
) {
    Column(modifier = Modifier.padding(top = 22.dp)) {
        Text(
            text = stringResource(R.string.select_an_preset),
            modifier = Modifier.padding(horizontal = 24.dp),
            color = AppTheme.colors.contentPrimary,
            style = AppTheme.typography.dialogTitle,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
        Spacer(Modifier.height(12.dp))

        // The shortcut dialog without presets keeps the quick split action
        if (presets.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .background(AppTheme.colors.surfaceLayer1)
            ) {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(items = presets, key = { it.id }) { preset ->
                        RenderListItem(
                            modifier = Modifier,
                            preset = preset,
                            type = RenderListType.PRESET,
                            showWindowShift = !noCaptionWindows,
                            showWindowType = preset.isWindowTypeVisible(noCaptionWindows, nativeSplit),
                            showMainWindow = preset.isMainWindowVisible(mainWindowAvailable, nativeSplit),
                            onClick = { onSelectPreset(preset) },
                            onLongClick = { _, _ -> }
                        )
                    }
                }
                TopShadow()
                BottomShadow(Modifier.align(Alignment.BottomCenter))
            }
        }

        ExtraActionRow(
            iconRes = R.drawable.ic_history,
            title = stringResource(R.string.last_launched),
            hint = lastLaunchedHint,
            onClick = onSelectLastLaunched
        )
        onSelectPresetPanel?.let { onClick ->
            ExtraActionRow(
                iconRes = R.drawable.ic_presets_grid,
                title = stringResource(R.string.preset_panel),
                hint = stringResource(R.string.preset_panel_shortcut_hint),
                onClick = onClick
            )
        }
        onSelectQuickSplit?.let { onClick ->
            ExtraActionRow(
                iconRes = R.drawable.ic_quick_split,
                title = stringResource(R.string.quick_split),
                hint = stringResource(R.string.quick_split_shortcut_hint),
                onClick = onClick
            )
        }
    }
}

@Composable
private fun ExtraActionRow(@DrawableRes iconRes: Int, title: String, hint: String, onClick: () -> Unit) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(start = 16.dp, end = 14.dp, top = 10.dp, bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceMenu),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = rememberPainterResource(iconRes),
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
    Spacer(Modifier.width(12.dp))
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            style = AppTheme.typography.cardTitle,
            color = AppTheme.colors.contentPrimary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = hint,
            style = AppTheme.typography.dialogSubtitle,
            color = AppTheme.colors.contentPrimary.copy(.5f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
    Spacer(Modifier.width(12.dp))
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = AppTheme.colors.contentPrimary.copy(.35f),
        modifier = Modifier.size(20.dp)
    )
}
