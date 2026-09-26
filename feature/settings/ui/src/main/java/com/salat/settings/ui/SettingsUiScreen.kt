package com.salat.settings.ui

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
import androidx.compose.ui.Modifier
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
import com.salat.settings.common.presentation.RenderSliderSetting
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.toDoubleString
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.ValueSlider
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.isCarBuildType

@Composable
internal fun SettingsUiScreen(
    state: SettingsUiViewModel.ViewState,
    sendAction: (SettingsUiViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.ui), onNavigateBack)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceSettings)
        ) {
            TopShadow()

            RenderSettingsContent {
                // UiScale
                RenderSettingsGroup {
                    RenderSliderSetting(
                        title = stringResource(R.string.interface_size),
                        value = state.uiScale.toDoubleString()
                    ) {
                        ValueSlider(
                            value = state.uiScale,
                            valueRange = 0.8f..MAX_UI_SCALE,
                            onValueChange = { newValue ->
                                sendAction(SettingsUiViewModel.Action.SetUiScale(newValue))
                            },
                            enabled = true,
                            defaultMark = DEFAULT_UI_SCALE,
                            step = 0.1f
                        )
                    }
                }

                RenderGroupDivider()

                RenderGroupCaption(stringResource(R.string.ui_scale_factor))

                Spacer(Modifier.height(14.dp))

                // ToolbarExtraSpace
                RenderSettingsGroup {
                    RenderSliderSetting(
                        title = stringResource(R.string.top_panel_indentation_title),
                        value = state.toolbarExtraSpace.toString()
                    ) {
                        ValueSlider(
                            value = state.toolbarExtraSpace,
                            valueRange = 0..MAX_TOOLBAR_EXTRA_SPACE,
                            onValueChange = { newValue ->
                                sendAction(SettingsUiViewModel.Action.SetToolbarExtraSpace(newValue))
                            },
                            defaultMark = if (isCarBuildType) {
                                BuildConfig.DEFAULT_CAR_TOOLBAR_EXTRA_SPACE
                            } else null,
                            enabled = true,
                            step = 1
                        )
                    }
                }

                RenderGroupDivider()

                RenderGroupCaption(stringResource(R.string.top_panel_indentation_desc))

                Spacer(Modifier.height(12.dp))

                // Launch History
                RenderSettingsGroup {
                    RenderGroupTitle(stringResource(R.string.launch_history))

                    RenderSwitcher(
                        title = stringResource(R.string.show_last_launched_split),
                        subtitle = stringResource(R.string.display_last_launched_split),
                        value = state.showLastLaunchedSplit,
                        groupDivider = false,
                        onChange = { sendAction(SettingsUiViewModel.Action.SetShowLastLaunchedSplit(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.contrast_border),
                        subtitle = stringResource(R.string.contrast_border_desc),
                        value = state.lastLaunchedSplitContrast,
                        groupDivider = false,
                        onChange = { sendAction(SettingsUiViewModel.Action.SetLastLaunchedSplitContrast(it)) }
                    )
                }

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsUiScreenPreview() {
    PreviewScreen {
        SettingsUiScreen(
            state = SettingsUiViewModel.ViewState()
        )
    }
}
