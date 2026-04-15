package com.salat.settings.darkScreenMode.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.components.AccessibilityServiceRequireDialog
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

@Composable
internal fun SettingsDarkScreenModeScreen(
    state: SettingsDarkScreenModeViewModel.ViewState,
    sendAction: (SettingsDarkScreenModeViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->

    var accessibilityServiceRequireDialog by rememberSaveable { mutableStateOf(false) }
    if (accessibilityServiceRequireDialog) {
        AccessibilityServiceRequireDialog(
            uiScaleState = state.uiScale,
            onConfirm = {
                sendAction(SettingsDarkScreenModeViewModel.Action.SetDarkScreenAutoClose(true))
                accessibilityServiceRequireDialog = false
            },
            onDismiss = { accessibilityServiceRequireDialog = false }
        )
    }

    fun toggleAutoClose(newValue: Boolean) {
        if (state.darkScreenAutoClose == true || state.accessibilityServiceEnabled) {
            sendAction(SettingsDarkScreenModeViewModel.Action.SetDarkScreenAutoClose(newValue))
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
        RenderToolbar(stringResource(R.string.dark_background), onNavigateBack)
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
                    RenderGroupTitle(stringResource(R.string.automation))

                    RenderSwitcher(
                        title = stringResource(R.string.auto_close),
                        subtitle = stringResource(R.string.auto_close_desc),
                        value = state.darkScreenAutoClose,
                        groupDivider = false,
                        onChange = { toggleAutoClose(it) }
                    )
                }

                RenderGroupDivider()
                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.appearance))

                    RenderSwitcher(
                        title = stringResource(R.string.back_button),
                        subtitle = stringResource(R.string.back_button_display),
                        value = state.darkScreenBackButton,
                        groupDivider = false,
                        onChange = {
                            sendAction(SettingsDarkScreenModeViewModel.Action.SetDarkScreenBackButton(it))
                        }
                    )
                }

                RenderGroupDivider()
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsDarkScreenModeScreenPreview() {
    PreviewScreen {
        SettingsDarkScreenModeScreen(
            state = SettingsDarkScreenModeViewModel.ViewState()
        )
    }
}
