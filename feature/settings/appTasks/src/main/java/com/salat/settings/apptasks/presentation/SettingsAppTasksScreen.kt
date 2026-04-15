package com.salat.settings.apptasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

@Composable
internal fun SettingsAppTasksScreen(
    state: SettingsAppTasksViewModel.ViewState,
    sendAction: (SettingsAppTasksViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.launch_with_tasks), onNavigateBack)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.music_aggregators))

                    RenderSwitcher(
                        title = stringResource(R.string.ym_compat_title),
                        subtitle = stringResource(R.string.ym_compat_desc),
                        value = state.ymCompatPlay,
                        groupDivider = false,
                        onChange = { sendAction(SettingsAppTasksViewModel.Action.SetYmCompatPlay(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.murglar_compat_title),
                        subtitle = stringResource(R.string.murglar_compat_desc),
                        value = state.murglarCompatPlay,
                        groupDivider = false,
                        onChange = { sendAction(SettingsAppTasksViewModel.Action.SetMurglarCompatPlay(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.vkx_compat_title),
                        subtitle = stringResource(R.string.vkx_compat_desc),
                        value = state.vkxCompatPlay,
                        groupDivider = false,
                        onChange = { sendAction(SettingsAppTasksViewModel.Action.SetVkxCompatPlay(it)) }
                    )
                }

                RenderGroupDivider()
            }
        }
    }
}

@Preview
@Composable
private fun SettingsAppTasksScreenPreview() {
    PreviewScreen {
        SettingsAppTasksScreen(
            state = SettingsAppTasksViewModel.ViewState()
        )
    }
}
