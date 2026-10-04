package com.salat.settings.quicksplit.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupCaption
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderSettingsContent
import com.salat.settings.common.presentation.RenderSettingsGroup
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.quicksplit.presentation.components.QuickSplitAppList
import com.salat.settings.quicksplit.presentation.components.QuickSplitAppsGrid
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.ui.rememberIsLandscape
import com.salat.ui.splitRatioLabel
import com.salat.uikit.component.BaseDialog
import com.salat.uikit.component.DialogButton
import com.salat.uikit.component.DialogButtonKind
import com.salat.uikit.component.DialogButtons
import com.salat.uikit.component.DialogTitle
import com.salat.uikit.component.DialogTopPadding
import com.salat.uikit.component.RatioGlyph
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.SettingsDefaults
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.capitalizeFirstLetter

private val TileShape = RoundedCornerShape(12.dp)

@Composable
internal fun SettingsQuickSplitScreen(
    state: SettingsQuickSplitViewModel.ViewState,
    uiScaleState: State<Float>? = null,
    sendAction: (SettingsQuickSplitViewModel.Action) -> Unit = {},
    onNavigateToDefaultRatio: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        val freeApps = remember(state.deviceApps, state.packages) {
            state.deviceApps.filter { it.packageName !in state.packages }
        }
        AddAppsDialog(
            apps = freeApps.takeIf { state.deviceApps.isNotEmpty() },
            uiScale = uiScaleState?.value,
            onDismiss = { showAddDialog = false },
            onAdd = { apps ->
                showAddDialog = false
                sendAction(SettingsQuickSplitViewModel.Action.AddApps(apps))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.quick_split), onNavigateBack)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceSettings)
        ) {
            TopShadow()

            RenderSettingsContent {
                RenderSettingsGroup {
                    RenderGroupTitle(stringResource(R.string.quick_split_default_ratio))

                    DefaultRatioButton(state = state, onClick = onNavigateToDefaultRatio)

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.quick_split_skip_side_step),
                        subtitle = stringResource(R.string.quick_split_skip_side_step_desc),
                        value = state.skipSideStep,
                        groupDivider = false,
                        onChange = { sendAction(SettingsQuickSplitViewModel.Action.SetSkipSideStep(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.quick_split_dark_background),
                        subtitle = stringResource(R.string.dark_background_split),
                        value = state.darkBackground,
                        groupDivider = false,
                        onChange = { sendAction(SettingsQuickSplitViewModel.Action.SetDarkBackground(it)) }
                    )
                }

                RenderGroupDivider()
                Spacer(Modifier.height(12.dp))

                RenderSettingsGroup {
                    RenderGroupTitle(stringResource(R.string.quick_split_apps))

                    state.apps?.let { apps ->
                        QuickSplitAppsGrid(
                            apps = apps,
                            onMove = { from, to -> sendAction(SettingsQuickSplitViewModel.Action.MoveApp(from, to)) },
                            onDrop = { sendAction(SettingsQuickSplitViewModel.Action.SaveAppsOrder) },
                            onRemove = { sendAction(SettingsQuickSplitViewModel.Action.RemoveApp(it.packageName)) },
                            onAdd = { showAddDialog = true },
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 16.dp)
                        )
                    }
                }

                RenderGroupDivider()

                RenderGroupCaption(stringResource(R.string.quick_split_apps_hint))

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun DefaultRatioButton(state: SettingsQuickSplitViewModel.ViewState, onClick: () -> Unit) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    val isLandscape = rememberIsLandscape()

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(TileShape)
            .background(AppTheme.colors.contentAccent.copy(.18f)),
        contentAlignment = Alignment.Center
    ) {
        RatioGlyph(
            firstShare = state.ratio,
            isLandscape = isLandscape,
            length = 28.dp,
            firstActive = state.insertFirst,
            secondActive = !state.insertFirst,
            firstColor = AppTheme.colors.settingsTitleAccent,
            secondColor = AppTheme.colors.settingsTitleAccent,
            inactiveColor = AppTheme.colors.settingsTitleAccent
        )
    }

    Spacer(Modifier.width(18.dp))

    Column(
        Modifier
            .weight(1f)
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = stringResource(
                R.string.quick_split_ratio_summary,
                splitRatioLabel(state.ratio),
                stringResource(newWindowSide(state.insertFirst, isLandscape))
            ),
            style = AppTheme.typography.screenTitle,
            color = AppTheme.colors.contentPrimary
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = if (state.showWindowTypes) windowTypesSummary(state, isLandscape) else {
                stringResource(R.string.quick_split_ratio_hint)
            },
            color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
            style = AppTheme.typography.dialogSubtitle
        )
    }

    Spacer(Modifier.width(12.dp))

    Text(
        text = stringResource(R.string.quick_split_configure),
        style = AppTheme.typography.radioTitle,
        color = AppTheme.colors.settingsTitleAccent,
        maxLines = 1
    )
    Icon(
        modifier = Modifier.size(24.dp),
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        tint = AppTheme.colors.settingsTitleAccent,
        contentDescription = null
    )
}

private fun newWindowSide(insertFirst: Boolean, isLandscape: Boolean) = when {
    insertFirst && isLandscape -> R.string.quick_split_side_left
    insertFirst -> R.string.quick_split_side_top
    isLandscape -> R.string.quick_split_side_right
    else -> R.string.quick_split_side_bottom
}

@Composable
private fun windowTypesSummary(state: SettingsQuickSplitViewModel.ViewState, isLandscape: Boolean): String {
    val firstWindow = stringResource(if (isLandscape) R.string.left_window else R.string.top_window)
    val secondWindow = stringResource(if (isLandscape) R.string.right_window else R.string.bottom_window)
    val caption = stringResource(R.string.window_type_caption).lowercase()
    val noCaption = stringResource(R.string.window_type_no_caption).lowercase()
    return stringResource(
        R.string.quick_split_window_types,
        firstWindow,
        if (state.firstCaption) caption else noCaption,
        secondWindow.lowercase(),
        if (state.secondCaption) caption else noCaption
    )
}

// A tap selects or unselects an app. The set gets the apps in the order of the taps
@Composable
private fun AddAppsDialog(
    apps: List<DisplayQuickSplitApp>?,
    uiScale: Float?,
    onDismiss: () -> Unit,
    onAdd: (List<DisplayQuickSplitApp>) -> Unit
) = BaseDialog(uiScaleState = uiScale, onDismiss = onDismiss) {
    var selectedApps by remember { mutableStateOf(emptyList<DisplayQuickSplitApp>()) }

    Column(modifier = Modifier.padding(top = DialogTopPadding)) {
        DialogTitle(stringResource(R.string.quick_split_choose_apps))
        Spacer(modifier = Modifier.height(16.dp))
        QuickSplitAppList(
            apps = apps,
            isSelected = { app -> selectedApps.any { it.packageName == app.packageName } },
            onClick = { app ->
                selectedApps = if (selectedApps.any { it.packageName == app.packageName }) {
                    selectedApps.filterNot { it.packageName == app.packageName }
                } else selectedApps + app
            }
        )
        Spacer(Modifier.height(4.dp))
        DialogButtons {
            DialogButton(
                text = stringResource(android.R.string.cancel).capitalizeFirstLetter(),
                onClick = onDismiss
            )
            DialogButton(
                text = if (selectedApps.isEmpty()) {
                    stringResource(R.string.add)
                } else stringResource(R.string.quick_split_add_apps, selectedApps.size),
                kind = DialogButtonKind.Accent,
                enabled = selectedApps.isNotEmpty(),
                onClick = { onAdd(selectedApps) }
            )
        }
    }
}

@Preview
@Composable
private fun SettingsQuickSplitScreenPreview() {
    PreviewScreen {
        SettingsQuickSplitScreen(
            state = SettingsQuickSplitViewModel.ViewState(skipSideStep = false)
        )
    }
}
