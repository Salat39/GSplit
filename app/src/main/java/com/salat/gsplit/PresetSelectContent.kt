package com.salat.gsplit

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salat.settings.list.presentation.components.PresetSelectDialog
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.uikit.theme.AppTheme

internal fun ComponentActivity.setPresetSelectContent(
    viewModel: ShortcutViewModel,
    @StringRes lastLaunchedHint: Int,
    onSelectPreset: (DisplaySplitPreset) -> Unit,
    onSelectLastLaunched: () -> Unit,
    onSelectPresetPanel: (() -> Unit)? = null,
    onSelectQuickSplit: (() -> Unit)? = null
) {
    setContent {
        val presets by viewModel.presetsState.collectAsStateWithLifecycle()
        val uiScale by viewModel.uiScaleState.collectAsStateWithLifecycle()
        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
        val noCaptionWindows by viewModel.noCaptionWindowsState.collectAsStateWithLifecycle()
        val mainWindowAvailable by viewModel.mainWindowAvailableState.collectAsStateWithLifecycle()
        val nativeSplit by viewModel.nativeSplitState.collectAsStateWithLifecycle()

        AppTheme(darkTheme = darkTheme) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                presets?.let {
                    // Without presets only the quick split action is useful
                    if (it.isEmpty() && onSelectQuickSplit == null) {
                        LaunchedEffect(Unit) { finish() }
                        return@let
                    }
                    PresetSelectDialog(
                        presets = it,
                        uiScale = uiScale,
                        lastLaunchedHint = stringResource(lastLaunchedHint),
                        onSelectPreset = onSelectPreset,
                        onSelectLastLaunched = onSelectLastLaunched,
                        onDismiss = ::finish,
                        onSelectPresetPanel = onSelectPresetPanel,
                        onSelectQuickSplit = onSelectQuickSplit,
                        noCaptionWindows = noCaptionWindows,
                        mainWindowAvailable = mainWindowAvailable,
                        nativeSplit = nativeSplit
                    )
                }
            }
        }
    }
}
