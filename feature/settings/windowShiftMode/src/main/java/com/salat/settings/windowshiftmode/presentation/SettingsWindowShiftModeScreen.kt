package com.salat.settings.windowshiftmode.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupCaption
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderSettingsContent
import com.salat.settings.common.presentation.RenderSettingsGroup
import com.salat.settings.common.presentation.RenderSliderSetting
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.components.AccessibilityServiceRequireDialog
import com.salat.settings.windowshiftmode.BuildConfig
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.ValueSlider
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.isCarBuildType

@Composable
internal fun SettingsWindowShiftModeScreen(
    state: SettingsWindowShiftModeViewModel.ViewState,
    sendAction: (SettingsWindowShiftModeViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    val isLandscape = rememberIsLandscape()

    var accessibilityServiceRequireDialog by rememberSaveable { mutableStateOf(false) }
    if (accessibilityServiceRequireDialog) {
        AccessibilityServiceRequireDialog(
            uiScaleState = state.uiScale,
            onConfirm = {
                sendAction(SettingsWindowShiftModeViewModel.Action.SetAutoRefocusWhenBottomWindowShift(true))
                accessibilityServiceRequireDialog = false
            },
            onDismiss = { accessibilityServiceRequireDialog = false }
        )
    }

    fun toggleWindowShift(newValue: Boolean) {
        if (state.autoRefocusWhenBottomWindowShift == true || state.accessibilityServiceEnabled) {
            sendAction(SettingsWindowShiftModeViewModel.Action.SetAutoRefocusWhenBottomWindowShift(newValue))
        } else {
            accessibilityServiceRequireDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.window_shift), onNavigateBack)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceSettings)
        ) {
            TopShadow()

            RenderSettingsContent {
                // BottomWindowShiftSize
                if (!isLandscape) {
                    RenderSettingsGroup {
                        RenderSliderSetting(
                            title = stringResource(R.string.bottom_window_shift_size_title),
                            value = state.bottomWindowShiftSize.toString()
                        ) {
                            ValueSlider(
                                value = state.bottomWindowShiftSize,
                                valueRange = 0..MAX_BOTTOM_WINDOW_SHIFT_SIZE,
                                onValueChange = { newValue ->
                                    sendAction(
                                        SettingsWindowShiftModeViewModel.Action.SetBottomWindowShiftSize(newValue)
                                    )
                                },
                                defaultMark = if (isCarBuildType) {
                                    BuildConfig.DEFAULT_CAR_BOTTOM_WINDOW_SHIFT_SIZE
                                } else null,
                                enabled = true,
                                step = 1
                            )
                        }
                    }

                    RenderGroupDivider()

                    RenderGroupCaption(stringResource(R.string.bottom_window_shift_size_desc))

                    Spacer(Modifier.height(10.dp))
                }

                RenderSettingsGroup {
                    RenderGroupTitle(stringResource(R.string.automation))

                    RenderSwitcher(
                        title = stringResource(R.string.keep_focus),
                        subtitle = stringResource(R.string.keep_focus_desc),
                        value = state.autoRefocusWhenBottomWindowShift,
                        groupDivider = false,
                        onChange = { toggleWindowShift(it) }
                    )

                    // RenderIconMenuDivider()
                }

                RenderGroupDivider()

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsWindowShiftModeScreenPreview() {
    PreviewScreen {
        SettingsWindowShiftModeScreen(
            state = SettingsWindowShiftModeViewModel.ViewState()
        )
    }
}
