package com.salat.settings.add.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.add.presentation.AddViewModel
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.DisplayFreeWindow
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.theme.AppTheme
import com.salat.uikit.theme.freeWindowColor

private const val RULER_STEPS = 10
private const val DOCK_GESTURE_ALPHA = .35f
private const val DOCK_FADE_DURATION = 200
private const val DOCK_EDGE_MARGIN = 32
private const val DOCK_SIDE_MARGIN = 16

@Composable
internal fun FreeModeEditor(
    windows: List<DisplayFreeWindow>,
    deviceApps: List<DeviceAppInfo>,
    showWindowType: Boolean,
    isWindowTypeLocked: Boolean,
    uiScaleState: State<Float>?,
    sendAction: (AddViewModel.Action) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var replaceWindowId by remember { mutableStateOf<Int?>(null) }
    var activeWindowGestures by remember { mutableIntStateOf(0) }
    val dockAlpha = animateFloatAsState(
        targetValue = if (activeWindowGestures > 0) DOCK_GESTURE_ALPHA else 1f,
        animationSpec = tween(DOCK_FADE_DURATION),
        label = "dockAlpha"
    )
    val isLandscape = rememberIsLandscape()

    if (showAddDialog) {
        val availableApps = remember(deviceApps, windows) { deviceApps.withoutAppsOf(windows) }
        AppSelectDialog(
            list = availableApps,
            uiScaleState = uiScaleState,
            onDismiss = { showAddDialog = false },
            onSelect = { app -> app?.let { sendAction(AddViewModel.Action.AddFreeWindow(it)) } }
        )
    }

    windows.find { it.id == replaceWindowId }?.let { window ->
        val availableApps = remember(deviceApps, windows, window.id) {
            deviceApps.withoutAppsOf(windows.filterNot { it.id == window.id })
        }
        AppSelectDialog(
            selected = window.app,
            list = availableApps,
            uiScaleState = uiScaleState,
            onDismiss = { replaceWindowId = null },
            onSelect = { app ->
                app?.let {
                    sendAction(
                        AddViewModel.Action.UpdateFreeWindow(
                            window.copy(app = it.copy(withCaption = window.app.withCaption))
                        )
                    )
                }
            }
        )
    }

    BoxWithConstraints(modifier.background(AppTheme.colors.surfaceLayer1)) {
        val canvasSize = IntSize(constraints.maxWidth, constraints.maxHeight)

        FreeModeRuler(Modifier.fillMaxSize())

        if (windows.isEmpty()) {
            Text(
                text = stringResource(R.string.free_mode_hint),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .background(AppTheme.colors.surfaceLayer1)
                    .padding(8.dp),
                style = AppTheme.typography.screenTitle,
                color = Color.White.copy(.6f),
                textAlign = TextAlign.Center
            )
        }

        // Pinned windows stay above the other windows after the launch
        val drawOrder = remember(windows) { windows.withIndex().sortedBy { it.value.alwaysOnTop } }
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            drawOrder.forEach { (index, window) ->
                key(window.id) {
                    FreeWindowFrame(
                        number = index + 1,
                        window = window,
                        otherWindows = windows.filterNot { it.id == window.id },
                        canvasSize = canvasSize,
                        color = freeWindowColor(index),
                        showWindowType = showWindowType,
                        isWindowTypeLocked = isWindowTypeLocked,
                        onChange = { sendAction(AddViewModel.Action.UpdateFreeWindow(it)) },
                        onGestureActive = { active -> activeWindowGestures += if (active) 1 else -1 },
                        onAppClick = { replaceWindowId = window.id },
                        onClose = { sendAction(AddViewModel.Action.RemoveFreeWindow(window.id)) }
                    )
                }
            }
        }

        FreeModeDock(
            isVertical = isLandscape,
            canSave = windows.isNotEmpty(),
            onBack = onBack,
            onAdd = { showAddDialog = true },
            onSave = { sendAction(AddViewModel.Action.CommitPreset) },
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterStart else Alignment.BottomCenter)
                .padding(
                    horizontal = if (isLandscape) DOCK_EDGE_MARGIN.dp else DOCK_SIDE_MARGIN.dp,
                    vertical = if (isLandscape) DOCK_SIDE_MARGIN.dp else DOCK_EDGE_MARGIN.dp
                )
                .graphicsLayer { alpha = dockAlpha.value }
        )
    }
}

private fun List<DeviceAppInfo>.withoutAppsOf(windows: List<DisplayFreeWindow>): List<DeviceAppInfo> {
    val usedPackages = windows.mapTo(HashSet()) { it.app.packageName }
    return filterNot { it.packageName in usedPackages }
}

@Composable
private fun FreeModeRuler(modifier: Modifier) {
    val textMeasurer = rememberTextMeasurer(cacheSize = RULER_STEPS)
    val labelStyle = AppTheme.typography.idTitle.copy(color = Color.White.copy(.55f))

    Canvas(modifier) {
        val stroke = 1.dp.toPx()
        val labelPadding = 4.dp.toPx()

        for (step in 1..RULER_STEPS) {
            val fraction = step.toFloat() / RULER_STEPS
            val lineColor = Color.White.copy(if (step == RULER_STEPS / 2) .22f else .08f)
            val label = textMeasurer.measure("${step * 100 / RULER_STEPS}%", labelStyle)

            val y = size.height * fraction
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), stroke)
            drawText(label, topLeft = Offset(labelPadding, y - label.size.height - labelPadding / 2))

            val x = size.width * fraction
            drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), stroke)
            drawText(label, topLeft = Offset(x - label.size.width - labelPadding, labelPadding))
        }
    }
}
