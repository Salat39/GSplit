package com.salat.settings.presets.presentation

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.salat.settings.common.presentation.toDecimalSecondString
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.ValueSlider
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

@Composable
internal fun SettingsPresetsScreen(
    state: SettingsPresetsViewModel.ViewState,
    sendAction: (SettingsPresetsViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.launching_presets), onNavigateBack)
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
                // General
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.general_behavior))

                    RenderSwitcher(
                        title = stringResource(R.string.minimize_by_run_title),
                        subtitle = stringResource(R.string.minimize_by_autorun_desc),
                        value = state.minimizeByStart,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetMinimizeByStart(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.minimize_by_autorun_title),
                        subtitle = stringResource(R.string.minimize_by_autorun_desc),
                        value = state.minimizeByAutostart,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetMinimizeByAutostart(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.autostart_minimize_delay_title),
                        subtitle = stringResource(R.string.autostart_minimize_delay_desc),
                        value = state.autoStartMinimizeDelay,
                        enable = state.minimizeByAutostart == true,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetAutoStartMinimizeDelay(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.soft_kill_app_title),
                        subtitle = stringResource(R.string.soft_kill_app_desc),
                        value = state.softKillApp,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetSoftKillApp(it)) }
                    )
                }

                RenderGroupDivider()
                Spacer(Modifier.height(16.dp))

                // BypassDelay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(
                        stringResource(R.string.multi_window_preparation)
                    )

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
                            value = state.bypassDelay,
                            valueRange = 0..MAX_BYPASS_DELAY,
                            onValueChange = { newValue ->
                                sendAction(SettingsPresetsViewModel.Action.SetBypassDelay(newValue))
                            },
                            enabled = true,
                            defaultMark = DEFAULT_BYPASS_DELAY,
                            step = 100
                        )

                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = state.bypassDelay.toDecimalSecondString(context),
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
                    text = stringResource(R.string.multi_window_prep_guide),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(10.dp))

                // SecondWindowDelay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(
                        stringResource(R.string.cross_launch_preparation)
                    )

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
                            value = state.secondWindowDelay,
                            valueRange = 0..MAX_SECOND_WINDOW_DELAY,
                            onValueChange = { newValue ->
                                sendAction(SettingsPresetsViewModel.Action.SetSecondWindowDelay(newValue))
                            },
                            enabled = true,
                            defaultMark = DEFAULT_SECOND_WINDOW_DELAY,
                            step = 100
                        )

                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = state.secondWindowDelay.toDecimalSecondString(context),
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
                    text = stringResource(R.string.cross_launch_prep_guide),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(10.dp))

                // AutoPlayDelay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(stringResource(R.string.autoplay))

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
                            value = state.autoPlayDelay,
                            valueRange = 0..MAX_AUTO_PLAY_DELAY,
                            onValueChange = { newValue ->
                                sendAction(SettingsPresetsViewModel.Action.SetAutoPlayDelay(newValue))
                            },
                            enabled = true,
                            defaultMark = DEFAULT_AUTO_PLAY_DELAY,
                            step = 100
                        )

                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = state.autoPlayDelay.toDecimalSecondString(context),
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
                    text = stringResource(R.string.autoplay_desc),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(10.dp))

                // Height corrector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderSliderTitle(
                        stringResource(R.string.height_corrector_title)
                    )

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ValueSlider(
                            modifier = Modifier.weight(1f),
                            value = state.heightCorrector,
                            valueRange = MIN_HEIGHT_CORRECTOR..MAX_HEIGHT_CORRECTOR,
                            onValueChange = { newValue ->
                                sendAction(SettingsPresetsViewModel.Action.SetHeightCorrector(newValue))
                            },
                            enabled = true,
                            defaultMark = DEFAULT_HEIGHT_CORRECTOR,
                            step = 1
                        )

                        val arrowDirection by remember(state.heightCorrector) {
                            derivedStateOf {
                                when {
                                    state.heightCorrector == 0 -> null
                                    state.heightCorrector > 0 -> true
                                    else -> false
                                }
                            }
                        }
                        Icon(
                            modifier = Modifier
                                .height(22.dp),
                            imageVector = if (arrowDirection == true) {
                                Icons.Filled.KeyboardArrowDown
                            } else Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = if (arrowDirection == null) Color.Transparent else AppTheme.colors.contentPrimary
                        )

                        Text(
                            modifier = Modifier.padding(start = 2.dp),
                            text = state.heightCorrector.toAnnotatedPaddedString(
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
                    text = stringResource(R.string.height_corrector_desc),
                    style = AppTheme.typography.aboutText,
                    color = AppTheme.colors.contentPrimary.copy(.4f)
                )

                Spacer(Modifier.height(10.dp))

                // Other
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.other))

                    RenderSwitcher(
                        title = stringResource(R.string.standby_mode),
                        subtitle = stringResource(R.string.standby_mode_description),
                        value = state.standbyMode,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetStandbyMode(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.context_adaptive_window_title),
                        subtitle = stringResource(R.string.context_adaptive_window_desc),
                        value = state.contextAdaptiveSizes,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetContextAdaptiveSizes(it)) }
                    )

                    RenderIconMenuDivider()

                    RenderSwitcher(
                        title = stringResource(R.string.exp_native_split_title),
                        subtitle = stringResource(R.string.exp_native_split_desc),
                        value = state.experimentalNativeSplit,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetExperimentalNativeSplit(it)) }
                    )
                }

                RenderGroupDivider()
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsPresetsScreenPreview() {
    PreviewScreen {
        SettingsPresetsScreen(
            state = SettingsPresetsViewModel.ViewState()
        )
    }
}
