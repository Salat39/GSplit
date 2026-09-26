package com.salat.gsplit

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salat.settings.list.presentation.components.PresetSelectDialog
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.ui.observeLifecycleFlow
import com.salat.uikit.theme.AppTheme

internal fun ComponentActivity.setPresetSelectContent(
    viewModel: ShortcutViewModel,
    @StringRes lastLaunchedHint: Int,
    onSelectPreset: (DisplaySplitPreset) -> Unit,
    onSelectLastLaunched: () -> Unit,
    onSelectPresetPanel: (() -> Unit)? = null
) {
    observeLifecycleFlow(viewModel.finishState) { finish() }

    setContent {
        val presets by viewModel.presetsState.collectAsStateWithLifecycle()
        val uiScale by viewModel.uiScaleState.collectAsStateWithLifecycle()
        val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
        val noCaptionWindows by viewModel.noCaptionWindowsState.collectAsStateWithLifecycle()

        AppTheme(darkTheme = darkTheme) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                presets?.let {
                    PresetSelectDialog(
                        presets = it,
                        uiScale = uiScale,
                        lastLaunchedHint = stringResource(lastLaunchedHint),
                        onSelectPreset = onSelectPreset,
                        onSelectLastLaunched = onSelectLastLaunched,
                        onDismiss = ::finish,
                        onSelectPresetPanel = onSelectPresetPanel,
                        showWindowShift = !noCaptionWindows
                    )
                }
            }
        }
    }
}
