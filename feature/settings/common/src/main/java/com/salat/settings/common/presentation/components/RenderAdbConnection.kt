package com.salat.settings.common.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salat.adb.data.entity.SHIZUKU_HELPER_PORT
import com.salat.adb.data.entity.TELNET_HELPER_PORT
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderSettingsGroup
import com.salat.settings.common.presentation.RenderSliderTitle
import com.salat.settings.common.presentation.entity.DisplayAdbState
import com.salat.uikit.component.HugeSegmentToggler
import com.salat.uikit.entity.SegmentTogglerItem
import com.salat.uikit.theme.AppTheme
import presentation.isCarBuildType

@Composable
fun RenderAdbConnection(
    connectionState: DisplayAdbState,
    enableAdbHelper: Boolean,
    adbHelperPort: Int,
    uiScale: Float?,
    onSelectPort: (Int) -> Unit,
    onDisable: () -> Unit
) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    RenderSettingsGroup {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdbStatusLamp(state = connectionState)

            Spacer(Modifier.width(20.dp))

            AdbStatusText(state = connectionState, modifier = Modifier.weight(1f))
        }
    }

    RenderGroupDivider()
    Spacer(Modifier.height(12.dp))

    RenderSettingsGroup {
        Spacer(Modifier.height(4.dp))
        RenderSliderTitle(
            stringResource(R.string.connection_port)
        )
        Spacer(Modifier.height(8.dp))

        AdbPortToggler(
            enableAdbHelper = enableAdbHelper,
            adbHelperPort = adbHelperPort,
            uiScale = uiScale,
            onSelectPort = onSelectPort,
            onDisable = onDisable,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(22.dp))
    }

    RenderGroupDivider()
}

@Composable
fun AdbStatusText(state: DisplayAdbState, modifier: Modifier = Modifier) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
    Text(
        text = when (state) {
            DisplayAdbState.Connected -> stringResource(R.string.connected)

            DisplayAdbState.Connecting -> stringResource(R.string.connecting)

            DisplayAdbState.Disconnected -> stringResource(R.string.disconnected)

            is DisplayAdbState.Error -> stringResource(R.string.error)
        },
        style = AppTheme.typography.statusTitle,
        color = AppTheme.colors.contentPrimary
    )

    if (state is DisplayAdbState.Error) {
        Text(
            text = state.message,
            style = AppTheme.typography.dialogSubtitle,
            color = AppTheme.colors.contentPrimary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}

@Composable
fun AdbPortToggler(
    enableAdbHelper: Boolean,
    adbHelperPort: Int,
    uiScale: Float?,
    onSelectPort: (Int) -> Unit,
    onDisable: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputPortDialog by remember { mutableStateOf(false) }
    if (inputPortDialog) {
        InputPortDialog(
            title = when (adbHelperPort) {
                7777, 5555, TELNET_HELPER_PORT, SHIZUKU_HELPER_PORT -> ""
                else -> adbHelperPort.toString()
            },
            uiScaleState = uiScale,
            onFinishInput = { newPort ->
                onSelectPort(newPort)
                inputPortDialog = false
            },
            onDismiss = {
                inputPortDialog = false
            }
        )
    }

    val offText = stringResource(R.string.off)
    val list = remember(adbHelperPort) {
        if (isCarBuildType) {
            listOf(
                SegmentTogglerItem(text = "Atlas", subtitle = "5555"),
                SegmentTogglerItem(text = "Preface", subtitle = "7777"),
                SegmentTogglerItem(
                    text = "Custom",
                    subtitle = when {
                        adbHelperPort != 7777 &&
                            adbHelperPort != 5555 &&
                            adbHelperPort != TELNET_HELPER_PORT &&
                            adbHelperPort != SHIZUKU_HELPER_PORT &&
                            adbHelperPort != -1 -> adbHelperPort.toString()

                        else -> null
                    }
                ),
                SegmentTogglerItem(text = "Telnet"),
                SegmentTogglerItem(text = "Shizuku"),
                SegmentTogglerItem(text = offText),
            )
        } else {
            listOf(
                SegmentTogglerItem(text = "5555"),
                SegmentTogglerItem(text = "7777"),
                SegmentTogglerItem(text = "Custom"),
                SegmentTogglerItem(text = "Telnet"),
                SegmentTogglerItem(text = "Shizuku"),
                SegmentTogglerItem(text = offText),
            )
        }
    }
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppTheme.colors.surfaceSettings.copy(.4f))
            .padding(2.dp)
    ) {
        if (adbHelperPort != -1) {
            HugeSegmentToggler(
                modifier = Modifier
                    .fillMaxWidth(),
                selectedIndex = when {
                    adbHelperPort == 5555 && enableAdbHelper -> 0
                    adbHelperPort == 7777 && enableAdbHelper -> 1
                    adbHelperPort == TELNET_HELPER_PORT && enableAdbHelper -> 3
                    adbHelperPort == SHIZUKU_HELPER_PORT && enableAdbHelper -> 4
                    adbHelperPort > 0 && enableAdbHelper -> 2
                    !enableAdbHelper -> 5
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
                    0 -> onSelectPort(5555)

                    1 -> onSelectPort(7777)

                    2 -> inputPortDialog = true

                    3 -> onSelectPort(TELNET_HELPER_PORT)

                    4 -> onSelectPort(SHIZUKU_HELPER_PORT)

                    5 -> onDisable()
                }
            }
        }
    }
}

@Composable
fun AdbStatusLamp(state: DisplayAdbState, modifier: Modifier = Modifier, lampSize: Dp = 80.dp) {
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
            .size(lampSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(lampSize * 0.075f)
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
