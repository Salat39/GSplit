package com.salat.settings.quicksplit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.ui.rememberIsLandscape
import com.salat.ui.scaledWithLayout
import com.salat.ui.splitRatioLabel
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.BaseIconButton
import com.salat.uikit.component.DialogButton
import com.salat.uikit.component.DialogButtons
import com.salat.uikit.component.DialogScrollArea
import com.salat.uikit.component.RatioGlyph
import com.salat.uikit.theme.AppTheme
import presentation.capitalizeFirstLetter

private const val DIALOG_DIM_AMOUNT = .6f
private val AppTileShape = RoundedCornerShape(16.dp)
private val AppTileMinWidth = 104.dp
private val AppIconSize = 56.dp

// Quick split step 2. The configured set shows as a grid. The full list has a search field
@Composable
fun QuickSplitAppStep(
    setApps: List<DisplayQuickSplitApp>,
    allApps: List<DisplayQuickSplitApp>?,
    showAllApps: Boolean,
    ratio: Float,
    insertFirst: Boolean,
    uiScale: Float,
    onBack: () -> Unit,
    onShowAllApps: () -> Unit,
    onSelect: (DisplayQuickSplitApp) -> Unit,
    onCancel: () -> Unit
) = BaseDialog(
    uiScaleState = uiScale,
    onDismiss = onBack,
    dimAmount = DIALOG_DIM_AMOUNT
) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BaseIconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = AppTheme.colors.contentPrimary,
                    contentDescription = null
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.quick_split_choose_app),
                modifier = Modifier.weight(1f),
                color = AppTheme.colors.contentPrimary,
                style = AppTheme.typography.dialogTitle,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
            Spacer(Modifier.width(12.dp))
            InsertSideBadge(ratio = ratio, insertFirst = insertFirst)
        }
        Spacer(Modifier.height(12.dp))

        if (showAllApps) {
            QuickSplitAppList(apps = allApps, onClick = onSelect)
        } else {
            DialogScrollArea(modifier = Modifier.weight(1f, fill = false)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(AppTileMinWidth),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items = setApps, key = { it.packageName }) { app ->
                        AppTile(app = app, onClick = { onSelect(app) })
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        DialogButtons {
            if (!showAllApps) {
                DialogButton(text = stringResource(R.string.quick_split_all_apps), onClick = onShowAllApps)
            }
            DialogButton(
                text = stringResource(android.R.string.cancel).capitalizeFirstLetter(),
                onClick = onCancel
            )
        }
    }
}

@Composable
private fun InsertSideBadge(ratio: Float, insertFirst: Boolean) = Row(
    modifier = Modifier
        .clip(CircleShape)
        .background(AppTheme.colors.contentPrimary.copy(.08f))
        .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    RatioGlyph(
        firstShare = ratio,
        isLandscape = rememberIsLandscape(),
        length = 20.dp,
        firstActive = insertFirst,
        secondActive = !insertFirst,
        inactiveColor = AppTheme.colors.contentPrimary.copy(.55f)
    )
    Text(
        text = splitRatioLabel(ratio),
        style = AppTheme.typography.radioTitle.copy(fontFeatureSettings = "tnum"),
        color = AppTheme.colors.contentPrimary,
        maxLines = 1
    )
}

@Composable
private fun AppTile(app: DisplayQuickSplitApp, onClick: () -> Unit) = Column(
    modifier = Modifier
        .clip(AppTileShape)
        .clickable(onClick = onClick)
        .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 14.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    DrawableImage(
        drawable = app.icon,
        modifier = Modifier
            .size(AppIconSize)
            .clip(RoundedCornerShape(14.dp))
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = app.appName,
        style = AppTheme.typography.radioTitle.scaledWithLayout(),
        color = AppTheme.colors.contentPrimary,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2
    )
}
