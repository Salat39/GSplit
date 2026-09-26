package com.salat.settings.add.presentation.components

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.add.presentation.entity.DisplayFreeWindow
import com.salat.settings.add.presentation.entity.FreeWindowEdge
import com.salat.settings.add.presentation.entity.FreeWindowLimits
import com.salat.ui.rememberPainterResource
import com.salat.ui.scaledWithLayout
import com.salat.uikit.theme.AppTheme
import kotlin.math.roundToInt

private const val HEADER_HEIGHT = 30
private const val MIN_WINDOW_SIZE = 180
private const val SNAP_DISTANCE = 8
private const val APP_ICON_SIZE = 40
private const val HANDLE_INSET = 6
private const val HANDLE_THICKNESS = 24
private const val HANDLE_LENGTH = 60
private const val HANDLE_INNER_GRAB = 14

private val windowShape = RoundedCornerShape(8.dp)
private val canvasEdges = listOf(0f, 1f)
private val isAlwaysOnTopSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@Composable
internal fun FreeWindowFrame(
    number: Int,
    window: DisplayFreeWindow,
    otherWindows: List<DisplayFreeWindow>,
    canvasSize: IntSize,
    color: Color,
    onChange: (DisplayFreeWindow) -> Unit,
    onGestureActive: (Boolean) -> Unit,
    onAppClick: () -> Unit,
    onClose: () -> Unit
) {
    val currentWindow = rememberUpdatedState(window)
    val currentOnChange by rememberUpdatedState(onChange)
    val currentOnGestureActive = rememberUpdatedState(onGestureActive)
    val density = LocalDensity.current
    val minSizePx = with(density) { MIN_WINDOW_SIZE.dp.toPx() }
    val snapPx = with(density) { SNAP_DISTANCE.dp.toPx() }
    val currentLimits by rememberUpdatedState(
        FreeWindowLimits(
            minWidth = minSizePx / canvasSize.width,
            minHeight = minSizePx / canvasSize.height,
            xLines = otherWindows.flatMap { listOf(it.left, it.right) } + canvasEdges,
            yLines = otherWindows.flatMap { listOf(it.top, it.bottom) } + canvasEdges,
            xSnap = snapPx / canvasSize.width,
            ySnap = snapPx / canvasSize.height
        )
    )
    val bounds = window.toPxRect(canvasSize)

    Column(
        modifier = Modifier
            .placeAt(bounds)
            .shadow(6.dp, windowShape)
            .clip(windowShape)
            .background(AppTheme.colors.surfaceBackground)
            .border(1.5.dp, color, windowShape)
            .dragFrom(currentWindow, currentOnGestureActive, canvasSize) { start, total ->
                currentOnChange(
                    start.moveBy(total.x / canvasSize.width, total.y / canvasSize.height, currentLimits)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(HEADER_HEIGHT.dp)
                .background(color)
                .systemGestureExclusion()
                .padding(start = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = number.toString(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(.25f))
                    .padding(horizontal = 7.dp, vertical = 1.dp),
                style = AppTheme.typography.toggleChip.scaledWithLayout(),
                color = Color.White
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = window.app.appName,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.cardFormatTitle.scaledWithLayout(),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (window.app.isMediaApp) {
                Spacer(Modifier.width(4.dp))
                HeaderToggle(
                    checked = window.app.autoPlay == true,
                    iconRes = R.drawable.ic_play,
                    label = stringResource(R.string.autoplay_short),
                    color = color,
                    onToggle = { onChange(window.copy(app = window.app.copy(autoPlay = it))) }
                )
            }

            if (isAlwaysOnTopSupported) {
                Spacer(Modifier.width(4.dp))
                HeaderToggle(
                    checked = window.alwaysOnTop,
                    iconRes = R.drawable.ic_pin,
                    label = stringResource(R.string.pip_short),
                    color = color,
                    onToggle = { onChange(window.copy(alwaysOnTop = it)) }
                )
            }

            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "close",
                tint = Color.White,
                modifier = Modifier
                    .size(HEADER_HEIGHT.dp)
                    .clickable(onClick = onClose)
                    .padding(6.dp)
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val iconModifier = Modifier
                    .size(APP_ICON_SIZE.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAppClick)
                window.app.icon?.let { icon ->
                    DrawableImage(drawable = icon, modifier = iconModifier)
                } ?: Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = window.app.appName,
                    tint = AppTheme.colors.contentWarning,
                    modifier = iconModifier.padding(8.dp)
                )

                Text(
                    text = "${bounds.width}x${bounds.height}",
                    style = AppTheme.typography.idTitle,
                    color = AppTheme.colors.contentPrimary.copy(.45f)
                )
            }

            FreeWindowEdge.entries.forEach { edge ->
                ResizeHandle(
                    edge = edge,
                    color = color,
                    gestureModifier = Modifier.dragFrom(
                        window = currentWindow,
                        onGestureActive = currentOnGestureActive,
                        key = canvasSize
                    ) { start, total ->
                        val delta = if (edge.isSide) total.x / canvasSize.width else total.y / canvasSize.height
                        currentOnChange(start.resize(edge, delta, currentLimits))
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderToggle(
    checked: Boolean,
    @DrawableRes iconRes: Int,
    label: String,
    color: Color,
    onToggle: (Boolean) -> Unit
) = Row(
    modifier = Modifier
        .clip(CircleShape)
        .background(if (checked) Color.White else Color.Transparent)
        .border(1.dp, Color.White.copy(if (checked) 1f else .7f), CircleShape)
        .clickable { onToggle(!checked) }
        .padding(horizontal = 7.dp, vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(
        painter = rememberPainterResource(iconRes),
        contentDescription = null,
        tint = if (checked) color else Color.White,
        modifier = Modifier.size(9.dp)
    )
    Spacer(Modifier.width(4.dp))
    Text(
        text = label,
        style = AppTheme.typography.idTitle.scaledWithLayout(),
        color = if (checked) color else Color.White,
        maxLines = 1
    )
}

@Composable
private fun BoxScope.ResizeHandle(edge: FreeWindowEdge, color: Color, gestureModifier: Modifier) = Box(
    modifier = Modifier
        .align(edge.alignment())
        // The inset keeps the handle away from the screen edge where the system back gesture starts
        .padding(HANDLE_INSET.dp)
        .size(
            width = if (edge.isSide) (HANDLE_THICKNESS + HANDLE_INNER_GRAB).dp else HANDLE_LENGTH.dp,
            height = if (edge.isSide) HANDLE_LENGTH.dp else (HANDLE_THICKNESS + HANDLE_INNER_GRAB).dp
        )
        .systemGestureExclusion()
        .then(gestureModifier),
    contentAlignment = edge.alignment()
) {
    Box(
        modifier = Modifier
            .size(
                width = if (edge.isSide) HANDLE_THICKNESS.dp else HANDLE_LENGTH.dp,
                height = if (edge.isSide) HANDLE_LENGTH.dp else HANDLE_THICKNESS.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(.85f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = edge.arrow(),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

// The total offset includes the touch slop so the dragged edge stays under the finger
private fun Modifier.dragFrom(
    window: State<DisplayFreeWindow>,
    onGestureActive: State<(Boolean) -> Unit>,
    key: Any,
    onDrag: (start: DisplayFreeWindow, total: Offset) -> Unit
) = pointerInput(key) {
    awaitEachGesture {
        val down = awaitFirstDown()
        val start = window.value
        val slopChange = awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
            ?: return@awaitEachGesture
        onGestureActive.value(true)
        try {
            var total = slopChange.position - down.position
            onDrag(start, total)
            drag(slopChange.id) { change ->
                total += change.positionChange()
                change.consume()
                onDrag(start, total)
            }
        } finally {
            onGestureActive.value(false)
        }
    }
}

private fun Modifier.placeAt(bounds: IntRect) = layout { measurable, _ ->
    val placeable = measurable.measure(
        Constraints.fixed(bounds.width.coerceAtLeast(0), bounds.height.coerceAtLeast(0))
    )
    layout(placeable.width, placeable.height) { placeable.place(bounds.left, bounds.top) }
}

// Each edge rounds separately. Two snapped edges then get the same pixel
private fun DisplayFreeWindow.toPxRect(canvasSize: IntSize) = IntRect(
    left = (left * canvasSize.width).roundToInt(),
    top = (top * canvasSize.height).roundToInt(),
    right = (right * canvasSize.width).roundToInt(),
    bottom = (bottom * canvasSize.height).roundToInt()
)

private val FreeWindowEdge.isSide
    get() = this == FreeWindowEdge.LEFT || this == FreeWindowEdge.RIGHT

private fun FreeWindowEdge.alignment() = when (this) {
    FreeWindowEdge.LEFT -> Alignment.CenterStart
    FreeWindowEdge.TOP -> Alignment.TopCenter
    FreeWindowEdge.RIGHT -> Alignment.CenterEnd
    FreeWindowEdge.BOTTOM -> Alignment.BottomCenter
}

private fun FreeWindowEdge.arrow() = when (this) {
    FreeWindowEdge.LEFT -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
    FreeWindowEdge.TOP -> Icons.Filled.KeyboardArrowUp
    FreeWindowEdge.RIGHT -> Icons.AutoMirrored.Filled.KeyboardArrowRight
    FreeWindowEdge.BOTTOM -> Icons.Filled.KeyboardArrowDown
}
