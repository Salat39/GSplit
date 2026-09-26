package com.salat.settings.presets.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.salat.settings.common.presentation.toDecimalSecondString
import com.salat.settings.common.presentation.toShiftString
import com.salat.settings.presets.presentation.components.ADVANCED_SETTINGS_ANIMATION_MS
import com.salat.settings.presets.presentation.components.AdvancedSettingsRow
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
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }

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

            RenderSettingsContent {
                // General
                RenderSettingsGroup {
                    RenderGroupTitle(stringResource(R.string.general_behavior))

                    RenderSwitcher(
                        title = stringResource(R.string.no_caption_windows_title),
                        subtitle = stringResource(R.string.no_caption_windows_desc),
                        value = state.noCaptionWindows,
                        groupDivider = false,
                        onChange = { sendAction(SettingsPresetsViewModel.Action.SetNoCaptionWindows(it)) }
                    )

                    RenderIconMenuDivider()

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

                    AnimatedVisibility(
                        visible = !advancedExpanded,
                        exit = shrinkVertically(animationSpec = tween(ADVANCED_SETTINGS_ANIMATION_MS)) +
                            fadeOut(tween(ADVANCED_SETTINGS_ANIMATION_MS))
                    ) {
                        Column {
                            RenderIconMenuDivider()
                            AdvancedSettingsRow(onClick = { advancedExpanded = true })
                        }
                    }
                }

                RenderGroupDivider()

                AnimatedVisibility(
                    visible = advancedExpanded,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(ADVANCED_SETTINGS_ANIMATION_MS)
                    ) + fadeIn(tween(ADVANCED_SETTINGS_ANIMATION_MS))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))

                        RenderSettingsGroup {
                            RenderSliderSetting(
                                title = stringResource(R.string.multi_window_preparation),
                                value = state.bypassDelay.toDecimalSecondString(context)
                            ) {
                                ValueSlider(
                                    value = state.bypassDelay,
                                    valueRange = 0..MAX_BYPASS_DELAY,
                                    onValueChange = { newValue ->
                                        sendAction(SettingsPresetsViewModel.Action.SetBypassDelay(newValue))
                                    },
                                    enabled = true,
                                    defaultMark = DEFAULT_BYPASS_DELAY,
                                    step = 100
                                )
                            }
                        }

                        RenderGroupDivider()

                        RenderGroupCaption(stringResource(R.string.multi_window_prep_guide))

                        Spacer(Modifier.height(10.dp))

                        RenderSettingsGroup {
                            RenderSliderSetting(
                                title = stringResource(R.string.cross_launch_preparation),
                                value = state.secondWindowDelay.toDecimalSecondString(context)
                            ) {
                                ValueSlider(
                                    value = state.secondWindowDelay,
                                    valueRange = 0..MAX_SECOND_WINDOW_DELAY,
                                    onValueChange = { newValue ->
                                        sendAction(SettingsPresetsViewModel.Action.SetSecondWindowDelay(newValue))
                                    },
                                    enabled = true,
                                    defaultMark = DEFAULT_SECOND_WINDOW_DELAY,
                                    step = 100
                                )
                            }
                        }

                        RenderGroupDivider()

                        RenderGroupCaption(stringResource(R.string.cross_launch_prep_guide))

                        Spacer(Modifier.height(10.dp))

                        RenderSettingsGroup {
                            RenderSliderSetting(
                                title = stringResource(R.string.autoplay),
                                value = state.autoPlayDelay.toDecimalSecondString(context)
                            ) {
                                ValueSlider(
                                    value = state.autoPlayDelay,
                                    valueRange = 0..MAX_AUTO_PLAY_DELAY,
                                    onValueChange = { newValue ->
                                        sendAction(SettingsPresetsViewModel.Action.SetAutoPlayDelay(newValue))
                                    },
                                    enabled = true,
                                    defaultMark = DEFAULT_AUTO_PLAY_DELAY,
                                    step = 100
                                )
                            }
                        }

                        RenderGroupDivider()

                        RenderGroupCaption(stringResource(R.string.autoplay_desc))

                        Spacer(Modifier.height(10.dp))

                        RenderSettingsGroup {
                            RenderSliderSetting(
                                title = stringResource(R.string.height_corrector_title),
                                value = state.heightCorrector.toShiftString()
                            ) {
                                ValueSlider(
                                    value = state.heightCorrector,
                                    valueRange = MIN_HEIGHT_CORRECTOR..MAX_HEIGHT_CORRECTOR,
                                    onValueChange = { newValue ->
                                        sendAction(SettingsPresetsViewModel.Action.SetHeightCorrector(newValue))
                                    },
                                    enabled = true,
                                    defaultMark = DEFAULT_HEIGHT_CORRECTOR,
                                    step = 1
                                )
                            }
                        }

                        RenderGroupDivider()

                        RenderGroupCaption(stringResource(R.string.height_corrector_desc))

                        Spacer(Modifier.height(10.dp))

                        RenderSettingsGroup {
                            RenderGroupTitle(stringResource(R.string.other))

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

                            RenderIconMenuDivider()

                            RenderSwitcher(
                                title = stringResource(R.string.exp_native_split_title),
                                subtitle = stringResource(R.string.exp_native_split_desc),
                                value = state.experimentalNativeSplit,
                                groupDivider = false,
                                onChange = {
                                    sendAction(SettingsPresetsViewModel.Action.SetExperimentalNativeSplit(it))
                                }
                            )
                        }

                        RenderGroupDivider()
                    }
                }

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
