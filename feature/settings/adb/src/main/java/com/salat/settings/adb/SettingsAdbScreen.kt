package com.salat.settings.adb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderSettingsContent
import com.salat.settings.common.presentation.RenderSettingsGroup
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.components.RenderAdbConnection
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsAdbScreen(
    state: SettingsAdbViewModel.ViewState,
    sendAction: (SettingsAdbViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.adb_features), onNavigateBack)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceSettings)
        ) {
            TopShadow()

            RenderSettingsContent {
                RenderAdbConnection(
                    connectionState = state.adbConnectionState,
                    enableAdbHelper = state.enableAdbHelper,
                    adbHelperPort = state.adbHelperPort,
                    uiScale = state.uiScale,
                    onSelectPort = { port ->
                        sendAction(SettingsAdbViewModel.Action.SetPort(port))
                        sendAction(SettingsAdbViewModel.Action.SetEnableAdbHelper(true))
                    },
                    onDisable = { sendAction(SettingsAdbViewModel.Action.SetEnableAdbHelper(false)) }
                )

                if (state.enableAdbHelper) {
                    Spacer(Modifier.height(12.dp))

                    RenderSettingsGroup {
                        RenderGroupTitle(stringResource(R.string.modes))

                        RenderSwitcher(
                            title = stringResource(R.string.force_kill_app_title),
                            subtitle = stringResource(R.string.force_kill_app_desc),
                            value = state.enableAdbForceStop,
                            groupDivider = false,
                            onChange = { sendAction(SettingsAdbViewModel.Action.SetEnableAdbForceStop(it)) }
                        )

                        if (state.taskResizeSupported) {
                            RenderIconMenuDivider()

                            RenderSwitcher(
                                title = stringResource(R.string.task_resize_app_title),
                                subtitle = stringResource(R.string.task_resize_app_desc),
                                value = state.enableAdbTaskResize,
                                groupDivider = false,
                                onChange = { sendAction(SettingsAdbViewModel.Action.SetEnableAdbTaskResize(it)) }
                            )
                        }

                        if (state.closeOldSplitWindowsSupported) {
                            RenderIconMenuDivider()

                            RenderSwitcher(
                                title = stringResource(R.string.close_old_split_windows_title),
                                subtitle = stringResource(R.string.close_old_split_windows_desc),
                                value = state.closeOldSplitWindows,
                                groupDivider = false,
                                onChange = { sendAction(SettingsAdbViewModel.Action.SetCloseOldSplitWindows(it)) }
                            )
                        }
                    }

                    RenderGroupDivider()
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsAdbScreenPreview() {
    PreviewScreen {
        SettingsAdbScreen(
            state = SettingsAdbViewModel.ViewState()
        )
    }
}
