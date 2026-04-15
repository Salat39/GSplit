package com.salat.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderSliderTitle
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.toAnnotatedPaddedString
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

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // UiScale
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(stringResource(R.string.interface_size))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
                            value = state.uiScale,
                            valueRange = 0.8f..MAX_UI_SCALE,
                            onValueChange = { newValue ->
                                sendAction(SettingsUiViewModel.Action.SetUiScale(newValue))
                            },
                            enabled = true,
                            defaultMark = DEFAULT_UI_SCALE,
                            step = 0.1f
                        )

                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = state.uiScale.toDoubleString(),
                            style = AppTheme.typography.screenTitle,
                            color = AppTheme.colors.contentPrimary,
                        )
                    }
                }

                RenderGroupDivider()

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    text = stringResource(R.string.ui_scale_factor),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(14.dp))

                // ToolbarExtraSpace
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(
                        stringResource(R.string.top_panel_indentation_title)
                    )

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
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

                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = state.toolbarExtraSpace.toAnnotatedPaddedString(
                                3,
                                '0',
                                AppTheme.colors.contentPrimary.copy(.15f)
                            ),
                            style = AppTheme.typography.screenTitle,
                            color = AppTheme.colors.contentPrimary,
                        )
                    }
                }

                RenderGroupDivider()

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    text = stringResource(R.string.top_panel_indentation_desc),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(12.dp))

                // Launch History
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
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
