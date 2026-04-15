package com.salat.settings.adb

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.adb.entity.DisplayAdbState
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderSliderTitle
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.uikit.component.HugeSegmentToggler
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.entity.SegmentTogglerItem
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.isCarBuildType

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

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                        .padding(start = 24.dp, end = 20.dp, top = 22.dp, bottom = 22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusLamp(state = state.adbConnectionState)

                    Spacer(Modifier.width(20.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            modifier = Modifier,
                            text = when (state.adbConnectionState) {
                                DisplayAdbState.Connected -> stringResource(R.string.connected)

                                DisplayAdbState.Connecting -> stringResource(R.string.connecting)

                                DisplayAdbState.Disconnected -> stringResource(R.string.disconnected)

                                is DisplayAdbState.Error -> stringResource(R.string.error)
                            },
                            style = AppTheme.typography.statusTitle,
                            color = AppTheme.colors.contentPrimary
                        )

                        val conState = state.adbConnectionState
                        if (conState is DisplayAdbState.Error) {
                            Text(
                                text = conState.message,
                                style = AppTheme.typography.dialogSubtitle,
                                color = AppTheme.colors.contentPrimary,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                        }
                    }
                }

                RenderGroupDivider()
                Spacer(Modifier.height(12.dp))

                // Input port dialog
                var inputPortDialog by remember { mutableStateOf(false) }
                if (inputPortDialog) {
                    InputPortDialog(
                        title = when (state.adbHelperPort) {
                            7777, 5555 -> ""
                            else -> state.adbHelperPort.toString()
                        },
                        uiScaleState = state.uiScale,
                        onFinishInput = { newPort ->
                            sendAction(SettingsAdbViewModel.Action.SetPort(newPort))
                            sendAction(SettingsAdbViewModel.Action.SetEnableAdbHelper(true))
                            inputPortDialog = false
                        },
                        onDismiss = {
                            inputPortDialog = false
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    Spacer(Modifier.height(4.dp))
                    RenderSliderTitle(
                        stringResource(R.string.connection_port)
                    )
                    Spacer(Modifier.height(8.dp))

                    val context = LocalContext.current
                    val offText = stringResource(R.string.off)
                    val list = remember(state.adbHelperPort) {
                        if (isCarBuildType) {
                            listOf(
                                SegmentTogglerItem(text = "Atlas", subtitle = "5555"),
                                SegmentTogglerItem(text = "Preface", subtitle = "7777"),
                                SegmentTogglerItem(
                                    text = context.getString(R.string.other),
                                    subtitle = when {
                                        state.adbHelperPort != 7777 &&
                                            state.adbHelperPort != 5555 &&
                                            state.adbHelperPort != -1 -> state.adbHelperPort.toString()

                                        else -> null
                                    }
                                ),
                                SegmentTogglerItem(text = offText),
                            )
                        } else {
                            listOf(
                                SegmentTogglerItem(text = "5555"),
                                SegmentTogglerItem(text = "7777"),
                                SegmentTogglerItem(text = context.getString(R.string.other)),
                                SegmentTogglerItem(text = offText),
                            )
                        }
                    }
                    Box(
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppTheme.colors.surfaceSettings.copy(.4f))
                            .padding(2.dp)
                    ) {
                        if (state.adbHelperPort != -1) {
                            HugeSegmentToggler(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                selectedIndex = when {
                                    state.adbHelperPort == 5555 && state.enableAdbHelper -> 0
                                    state.adbHelperPort == 7777 && state.enableAdbHelper -> 1
                                    state.adbHelperPort > 0 && state.enableAdbHelper -> 2
                                    !state.enableAdbHelper -> 3
                                    else -> 0
                                },
                                fontSize = 14,
                                activeBackground = AppTheme.colors.contentAccent,
                                itemContentColor = AppTheme.colors.contentPrimary,
                                items = list,
                                onReSelect = {
                                    if (it == 2) inputPortDialog = true
                                }
                            ) {
                                when (it) {
                                    0 -> {
                                        sendAction(SettingsAdbViewModel.Action.SetPort(5555))
                                        sendAction(SettingsAdbViewModel.Action.SetEnableAdbHelper(true))
                                    }

                                    1 -> {
                                        sendAction(SettingsAdbViewModel.Action.SetPort(7777))
                                        sendAction(SettingsAdbViewModel.Action.SetEnableAdbHelper(true))
                                    }

                                    2 -> inputPortDialog = true

                                    3 -> sendAction(SettingsAdbViewModel.Action.SetEnableAdbHelper(false))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                }

                RenderGroupDivider()

                if (state.enableAdbHelper) {
                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.surfaceSettingsLayer1)
                    ) {
                        RenderGroupTitle(stringResource(R.string.modes))

                        RenderSwitcher(
                            title = stringResource(R.string.force_kill_app_title),
                            subtitle = stringResource(R.string.force_kill_app_desc),
                            value = state.enableAdbForceStop,
                            groupDivider = false,
                            onChange = { sendAction(SettingsAdbViewModel.Action.SetEnableAdbForceStop(it)) }
                        )

                        RenderIconMenuDivider()

                        RenderSwitcher(
                            title = stringResource(R.string.direct_window_control),
                            subtitle = stringResource(R.string.adb_overlay_features_desc),
                            value = state.enableAdbOverlayFun,
                            groupDivider = false,
                            onChange = { sendAction(SettingsAdbViewModel.Action.SetEnableAdbOverlayFun(it)) }
                        )
                    }

                    RenderGroupDivider()
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusLamp(state: DisplayAdbState, modifier: Modifier = Modifier) {
    val targetColor = when (state) {
        DisplayAdbState.Connected -> AppTheme.colors.statusSuccess
        DisplayAdbState.Connecting -> AppTheme.colors.statusWarning
        DisplayAdbState.Disconnected -> AppTheme.colors.statusDisabled
        is DisplayAdbState.Error -> AppTheme.colors.statusError
    }

    val baseColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 350),
        label = "lampColor"
    )

    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(6.dp)
            .clip(CircleShape)
            .drawWithCache {
                val radius = size.minDimension / 2f

                val volumeBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to baseColor.copy(alpha = 1.0f),
                        0.55f to baseColor.copy(alpha = 0.95f),
                        1.0f to baseColor.copy(alpha = 1.0f)
                    ),
                    center = Offset(x = size.width * 0.35f, y = size.height * 0.30f),
                    radius = radius * 1.25f
                )

                val vignetteBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.65f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.22f)
                    ),
                    center = Offset(x = size.width / 2f, y = size.height / 2f),
                    radius = radius
                )

                val specularBrush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color.White.copy(alpha = 0.3f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(x = size.width * 0.28f, y = size.height * 0.22f),
                    radius = radius * 0.55f
                )

                onDrawBehind {
                    drawCircle(brush = volumeBrush)
                    drawCircle(brush = specularBrush)
                    drawCircle(brush = vignetteBrush)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = radius * 0.98f,
                        style = Stroke(width = radius * 0.06f)
                    )
                }
            }
    )
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
