package com.salat.settings.autostart.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.autostart.BuildConfig
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderSliderTitle
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.components.AccessibilityServiceRequireDialog
import com.salat.settings.common.presentation.toSecondString
import com.salat.uikit.component.RenderSettingsButton
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.ValueSlider
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

@Composable
internal fun SettingsAutostartScreen(
    state: SettingsAutostartViewModel.ViewState,
    sendAction: (SettingsAutostartViewModel.Action) -> Unit = {},
    onNavigateToScheduler: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->

    val context = LocalContext.current

    var accessibilityServiceConfirmDialog by rememberSaveable { mutableStateOf(false) }
    if (accessibilityServiceConfirmDialog) {
        AccessibilityServiceRequireDialog(
            titleRes = R.string.enable_AccessibilityService_step_one,
            confirmTitleRes = R.string.turn_on_auto_launch,
            uiScaleState = state.uiScale,
            onConfirm = {
                sendAction(SettingsAutostartViewModel.Action.SetSelfAutostart(true))
                accessibilityServiceConfirmDialog = false
            },
            onDismiss = { accessibilityServiceConfirmDialog = false }
        )
    }

    fun toggleAutoBoot(newValue: Boolean) {
        if (BuildConfig.BOOT_VIA_ACCESSIBILITY_SERVICE) {
            if (state.selfAutostart == true || state.accessibilityServiceEnabled) {
                sendAction(SettingsAutostartViewModel.Action.SetSelfAutostart(newValue))
            } else {
                accessibilityServiceConfirmDialog = true
            }
        } else {
            sendAction(SettingsAutostartViewModel.Action.SetSelfAutostart(newValue))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.autostart), onNavigateBack)
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
                // AutoStart
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.startup_conditions))

                    RenderSwitcher(
                        title = stringResource(R.string.self_autostart_title),
                        subtitle = stringResource(R.string.self_autostart_desc),
                        value = state.selfAutostart,
                        groupDivider = false,
                        onChange = { toggleAutoBoot(it) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.self_autostart_in_bg_title),
                        subtitle = stringResource(R.string.self_autostart_in_bg_desc),
                        value = state.selfAutostartInBg,
                        enable = state.selfAutostart == true,
                        groupDivider = false,
                        onChange = { sendAction(SettingsAutostartViewModel.Action.SetSelfAutostartInBg(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.self_autostart_by_connect_title),
                        subtitle = stringResource(R.string.self_autostart_by_connect_desc),
                        value = state.selfAutostartByConnect,
                        enable = state.selfAutostart == true,
                        groupDivider = false,
                        onChange = { sendAction(SettingsAutostartViewModel.Action.SetSelfAutostartByConnect(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSettingsButton(
                        title = stringResource(R.string.launch_scheduler_title),
                        subtitle = stringResource(R.string.launch_scheduler_desc),
                        enable = state.selfAutostart == true,
                        onClick = onNavigateToScheduler
                    )
                }

                RenderGroupDivider()

                Spacer(Modifier.height(16.dp))

                // AutostartDelay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(stringResource(R.string.self_autostart_delay_title))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
                            value = state.autostartDelay,
                            valueRange = 0..MAX_AUTOSTART_DELAY,
                            onValueChange = { newValue ->
                                sendAction(SettingsAutostartViewModel.Action.SetAutostartDelay(newValue))
                            },
                            defaultMark = DEFAULT_AUTOSTART_DELAY,
                            enabled = true,
                            step = 1000
                        )

                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = state.autostartDelay.toSecondString(context),
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
                    text = stringResource(R.string.self_autostart_delay_desc),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsAutostartScreenPreview() {
    PreviewScreen {
        SettingsAutostartScreen(
            state = SettingsAutostartViewModel.ViewState()
        )
    }
}
