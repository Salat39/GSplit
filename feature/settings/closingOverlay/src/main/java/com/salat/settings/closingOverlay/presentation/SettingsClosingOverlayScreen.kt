package com.salat.settings.closingOverlay.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.salat.preferences.BuildConfig
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderSliderTitle
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.settings.common.presentation.components.AccessibilityServiceRequireDialog
import com.salat.settings.common.presentation.toDecimalSecondString
import com.salat.settings.common.presentation.toDoubleString
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.ConfirmDialog
import com.salat.uikit.component.RenderSettingsButton
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.ValueSlider
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.toast

private const val MAX_SCALE = 3.5f

@Composable
internal fun SettingsClosingOverlayScreen(
    state: SettingsClosingOverlayViewModel.ViewState,
    sendAction: (SettingsClosingOverlayViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    val context = LocalContext.current

    var accessibilityServiceRequireDialog by rememberSaveable { mutableStateOf(false) }
    if (accessibilityServiceRequireDialog) {
        AccessibilityServiceRequireDialog(
            uiScaleState = state.uiScale,
            onConfirm = {
                sendAction(SettingsClosingOverlayViewModel.Action.SetOverlayEnabled(true))
                accessibilityServiceRequireDialog = false
            },
            onDismiss = { accessibilityServiceRequireDialog = false }
        )
    }

    fun toggleEnableOverlay(newValue: Boolean) {
        if (state.overlayEnabled == true || state.accessibilityServiceEnabled) {
            sendAction(SettingsClosingOverlayViewModel.Action.SetOverlayEnabled(newValue))
        } else {
            accessibilityServiceRequireDialog = true
        }
    }

    var resetPositionDialog by remember { mutableStateOf(false) }
    if (resetPositionDialog) {
        val toastMessage = stringResource(R.string.position_reset)
        ConfirmDialog(
            title = stringResource(R.string.reset_position),
            message = stringResource(R.string.confirm_reset_overlay_position),
            uiScale = state.uiScale,
            negativeAction = true,
            onCancel = { resetPositionDialog = false },
            onDismiss = { resetPositionDialog = false },
            onClick = {
                context.toast(toastMessage)
                sendAction(SettingsClosingOverlayViewModel.Action.ResetOverlayPosition)
                resetPositionDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.split_close), onNavigateBack)
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
                    RenderGroupTitle(stringResource(R.string.general))

                    RenderSwitcher(
                        title = stringResource(R.string.enable_overlay),
                        subtitle = stringResource(R.string.overlay_button_close_desc),
                        value = state.overlayEnabled,
                        groupDivider = false,
                        onChange = { toggleEnableOverlay(it) }
                    )

                    if (state.overlayEnabled == true) {
                        RenderIconMenuDivider()

                        RenderSwitcher(
                            title = stringResource(R.string.window_shift_before_closing),
                            subtitle = stringResource(R.string.window_shift_desc),
                            value = state.closeWindowDodgeSystemGes,
                            groupDivider = false,
                            onChange = {
                                sendAction(SettingsClosingOverlayViewModel.Action.SetCloseWindowDodgeSystemGes(it))
                            }
                        )

                        RenderIconMenuDivider()

                        RenderSwitcher(
                            title = stringResource(R.string.action_overlay_pin),
                            subtitle = stringResource(R.string.action_overlay_pin_description),
                            value = state.overlayLock,
                            groupDivider = false,
                            onChange = {
                                sendAction(SettingsClosingOverlayViewModel.Action.SetOverlayLock(it))
                            }
                        )

                        RenderIconMenuDivider()

                        RenderSettingsButton(
                            title = stringResource(R.string.reset_position),
                            subtitle = stringResource(R.string.reset_position_desc),
                            enable = true,
                            onClick = { resetPositionDialog = true }
                        )
                    }
                }

                RenderGroupDivider()

                if (state.overlayEnabled == true) {
                    Spacer(Modifier.height(12.dp))

                    // Size
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.surfaceSettingsLayer1)
                    ) {
                        RenderSliderTitle(
                            stringResource(R.string.overlay_size)
                        )

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ValueSlider(
                                modifier = Modifier.weight(1f),
                                value = state.overlayScale,
                                valueRange = 0.3f..MAX_SCALE,
                                onValueChange = { newValue ->
                                    sendAction(SettingsClosingOverlayViewModel.Action.SetOverlayScale(newValue))
                                },
                                defaultMark = BuildConfig.OVERLAY_SCALE,
                                enabled = true,
                                step = 0.05f
                            )

                            Text(
                                modifier = Modifier.padding(start = 10.dp),
                                text = state.overlayScale.toDoubleString(2),
                                style = AppTheme.typography.screenTitle,
                                color = AppTheme.colors.contentPrimary,
                            )
                        }
                    }

                    RenderGroupDivider()
                    Spacer(Modifier.height(12.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        text = stringResource(R.string.preview),
                        style = AppTheme.typography.aboutText,
                        textAlign = TextAlign.Center,
                        color = AppTheme.colors.contentPrimary.copy(.4f)
                    )
                    Spacer(Modifier.height(12.dp))

                    // Preview
                    val density = LocalDensity.current
                    val scaledDensity = remember(density, state.overlayScale) {
                        val realScale = (state.overlayScale / state.uiScale)
                        Density(
                            density.density * realScale,
                            density.fontScale * realScale
                        )
                    }
                    CompositionLocalProvider(
                        LocalDensity provides scaledDensity,
                        LocalLayoutDirection provides LayoutDirection.Ltr
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(state.overlayBgAlpha))
                                .padding(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .alpha(state.overlayIconAlpha)
                                    .size(26.dp),
                                // imageVector = Icons.Filled.Close,
                                painter = rememberPainterResource(R.drawable.ic_close4),
                                tint = Color.White,
                                contentDescription = null
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Bg alpha
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.surfaceSettingsLayer1)
                    ) {
                        RenderSliderTitle(
                            stringResource(R.string.background_transparency)
                        )

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ValueSlider(
                                modifier = Modifier.weight(1f),
                                value = state.overlayBgAlpha,
                                valueRange = 0f..1f,
                                onValueChange = { newValue ->
                                    sendAction(SettingsClosingOverlayViewModel.Action.SetOverlayBgAlpha(newValue))
                                },
                                defaultMark = .8f,
                                enabled = true,
                                step = 0.05f
                            )

                            Text(
                                modifier = Modifier.padding(start = 10.dp),
                                text = state.overlayBgAlpha.toDoubleString(2),
                                style = AppTheme.typography.screenTitle,
                                color = AppTheme.colors.contentPrimary,
                            )
                        }
                    }

                    RenderGroupDivider()
                    Spacer(Modifier.height(12.dp))

                    // Icon alpha
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.surfaceSettingsLayer1)
                    ) {
                        RenderSliderTitle(
                            stringResource(R.string.icon_transparency)
                        )

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ValueSlider(
                                modifier = Modifier.weight(1f),
                                value = state.overlayIconAlpha,
                                valueRange = 0f..1f,
                                onValueChange = { newValue ->
                                    sendAction(SettingsClosingOverlayViewModel.Action.SetOverlayIconAlpha(newValue))
                                },
                                defaultMark = .9f,
                                enabled = true,
                                step = 0.05f
                            )

                            Text(
                                modifier = Modifier.padding(start = 10.dp),
                                text = state.overlayIconAlpha.toDoubleString(2),
                                style = AppTheme.typography.screenTitle,
                                color = AppTheme.colors.contentPrimary,
                            )
                        }
                    }

                    RenderGroupDivider()
                    Spacer(Modifier.height(18.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.surfaceSettingsLayer1)
                    ) {
                        RenderGroupTitle(stringResource(R.string.other))

                        RenderSwitcher(
                            title = stringResource(R.string.sequential_closing),
                            subtitle = stringResource(R.string.sequential_closing_desc),
                            value = state.closeWindowSequential,
                            groupDivider = false,
                            onChange = {
                                sendAction(SettingsClosingOverlayViewModel.Action.SetCloseWindowSequential(it))
                            }
                        )
                    }

                    RenderGroupDivider()
                    Spacer(Modifier.height(12.dp))

                    // Window closing extra pause
                    if (state.closeWindowSequential == false) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppTheme.colors.surfaceSettingsLayer1)
                        ) {
                            RenderSliderTitle(
                                stringResource(R.string.window_closing_pause)
                            )

                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ValueSlider(
                                    modifier = Modifier.weight(1f),
                                    value = state.windowClosingExtraPause,
                                    valueRange = 0..2000,
                                    onValueChange = { newValue ->
                                        sendAction(
                                            SettingsClosingOverlayViewModel.Action.SetWindowClosingExtraPause(
                                                newValue
                                            )
                                        )
                                    },
                                    defaultMark = 100,
                                    enabled = true,
                                    step = 10
                                )

                                Text(
                                    modifier = Modifier.padding(start = 10.dp),
                                    text = state.windowClosingExtraPause.toDecimalSecondString(context, 2),
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
                            text = stringResource(R.string.window_closing_pause_desc),
                            style = AppTheme.typography.aboutText,
                            color = AppTheme.colors.contentPrimary.copy(.4f)
                        )
                    }
                }
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsClosingOverlayScreenPreview() {
    PreviewScreen {
        SettingsClosingOverlayScreen(
            state = SettingsClosingOverlayViewModel.ViewState()
        )
    }
}
