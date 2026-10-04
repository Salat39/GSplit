package com.salat.settings.quicksplit.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.zIndex
import com.salat.resources.R
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.ui.scaledWithLayout
import com.salat.uikit.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val TileShape = RoundedCornerShape(16.dp)
private val AppIconShape = RoundedCornerShape(14.dp)
private val AppIconSize = 52.dp
private val GridGap = 4.dp
private val MinCellWidth = 96.dp
private val RemoveBadgeSize = 24.dp
private val RemoveBadgeRing = 2.dp
private val RemoveBadgeTouchSize = 40.dp
private const val MIN_COLUMNS = 3
private const val DRAGGED_SCALE = 1.1f
private const val ADD_TILE_KEY = ""
private val PlacementSpec = spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset(1, 1))

// The set order is the quick split order. A long press starts the drag, a swipe scrolls the page
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickSplitAppsGrid(
    apps: List<DisplayQuickSplitApp>,
    onMove: (fromPackage: String, toPackage: String) -> Unit,
    onDrop: () -> Unit,
    onRemove: (DisplayQuickSplitApp) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) = BoxWithConstraints(modifier) {
    val dragState = rememberAppsDragState(onMove, onDrop)
    val columns = ((maxWidth + GridGap) / (MinCellWidth + GridGap)).toInt().coerceAtLeast(MIN_COLUMNS)
    val cellWidth = (maxWidth - GridGap * (columns - 1)) / columns

    FlowRow(
        maxItemsInEachRow = columns,
        horizontalArrangement = Arrangement.spacedBy(GridGap),
        verticalArrangement = Arrangement.spacedBy(GridGap)
    ) {
        apps.forEach { app ->
            key(app.packageName) {
                AppTile(
                    app = app,
                    dragState = dragState,
                    cellWidth = cellWidth,
                    onRemove = { onRemove(app) }
                )
            }
        }
        AddTile(dragState = dragState, cellWidth = cellWidth, onClick = onAdd)
    }
}

@Composable
private fun AppTile(app: DisplayQuickSplitApp, dragState: AppsDragState, cellWidth: Dp, onRemove: () -> Unit) {
    val key = app.packageName
    val haptic = LocalHapticFeedback.current
    val isDragged = dragState.draggedKey == key

    DisposableEffect(key) { onDispose { dragState.forget(key) } }

    Column(
        modifier = Modifier
            .zIndex(if (isDragged) 1f else 0f)
            .width(cellWidth)
            .animatedSlot(dragState, key)
            .pointerInput(dragState, key) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        if (dragState.onDragStart(key)) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragState.onDrag(key, dragAmount)
                    },
                    onDragEnd = { dragState.onDragEnd(key) },
                    onDragCancel = { dragState.onDragEnd(key) }
                )
            }
            .offset { dragState.offset(key) }
            .graphicsLayer {
                val scale = if (isDragged) DRAGGED_SCALE else 1f
                scaleX = scale
                scaleY = scale
            }
            .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            DrawableImage(
                drawable = app.icon,
                modifier = Modifier
                    .size(AppIconSize)
                    .clip(AppIconShape)
            )
            RemoveBadge(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = RemoveBadgeTouchSize / 2 - 3.dp, y = 3.dp - RemoveBadgeTouchSize / 2)
            )
        }
        Spacer(Modifier.height(8.dp))
        TileTitle(text = app.appName)
    }
}

// The badge has a ring of the card color, so it looks cut out of the icon corner
@Composable
private fun RemoveBadge(onClick: () -> Unit, modifier: Modifier) = Box(
    modifier = modifier
        .size(RemoveBadgeTouchSize)
        .clip(CircleShape)
        .clickable(onClick = onClick, role = Role.Button),
    contentAlignment = Alignment.Center
) {
    val cardColor = AppTheme.colors.surfaceSettingsLayer1
    Box(
        modifier = Modifier
            .size(RemoveBadgeSize + RemoveBadgeRing * 2)
            .clip(CircleShape)
            .background(cardColor)
            .padding(RemoveBadgeRing)
            .clip(CircleShape)
            .background(AppTheme.colors.contentPrimary.copy(.12f).compositeOver(cardColor)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun AddTile(dragState: AppsDragState, cellWidth: Dp, onClick: () -> Unit) = Column(
    modifier = Modifier
        .width(cellWidth)
        .animatedSlot(dragState, ADD_TILE_KEY)
        .offset { dragState.offset(ADD_TILE_KEY) }
        .clip(TileShape)
        .clickable(onClick = onClick)
        .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    val strokeColor = AppTheme.colors.contentPrimary.copy(.4f)
    Box(
        modifier = Modifier
            .size(AppIconSize)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(14.dp.toPx() - strokeWidth / 2),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = AppTheme.colors.contentPrimary.copy(.7f),
            modifier = Modifier.size(26.dp)
        )
    }
    Spacer(Modifier.height(8.dp))
    TileTitle(text = stringResource(R.string.add), color = AppTheme.colors.settingsTitleAccent)
}

@Composable
private fun TileTitle(text: String, color: Color = AppTheme.colors.contentPrimary) = Text(
    text = text,
    style = AppTheme.typography.radioTitle.copy(fontSize = 13.sp, lineHeight = 16.sp).scaledWithLayout(),
    color = color,
    textAlign = TextAlign.Center,
    overflow = TextOverflow.Ellipsis,
    maxLines = 1
)

private fun Modifier.animatedSlot(dragState: AppsDragState, key: String) = onPlaced { coordinates ->
    dragState.onPlaced(key, IntRect(coordinates.positionInParent().round(), coordinates.size))
}

@Stable
private class AppsDragState(
    private val scope: CoroutineScope,
    private val onMove: (fromKey: String, toKey: String) -> Unit,
    private val onDrop: () -> Unit
) {
    var draggedKey by mutableStateOf<String?>(null)
        private set

    private var dragPosition by mutableStateOf(Offset.Zero)
    private val slots = mutableMapOf<String, IntRect>()
    private val positions = mutableMapOf<String, Animatable<IntOffset, AnimationVector2D>>()
    private var awaitedSlot: IntRect? = null
    private var moved = false

    // Other tiles slide from the old slot to the new slot. The dragged tile stays under the finger
    fun offset(key: String): IntOffset {
        val slot = slots[key] ?: return IntOffset.Zero
        val position = if (key == draggedKey) dragPosition.round() else positions[key]?.value ?: slot.topLeft
        return position - slot.topLeft
    }

    fun onPlaced(key: String, slot: IntRect) {
        slots[key] = slot
        val position = positions.getOrPut(key) { Animatable(slot.topLeft, IntOffset.VectorConverter) }
        if (key != draggedKey && position.targetValue != slot.topLeft) {
            scope.launch { position.animateTo(slot.topLeft, PlacementSpec) }
        }
    }

    fun onDragStart(key: String): Boolean {
        if (draggedKey != null) return false
        val slot = slots[key] ?: return false
        dragPosition = (positions[key]?.value ?: slot.topLeft).toOffset()
        awaitedSlot = null
        moved = false
        draggedKey = key
        return true
    }

    fun onDrag(key: String, delta: Offset) {
        if (key != draggedKey) return
        dragPosition += delta
        moveIfCrossed(key)
    }

    fun onDragEnd(key: String) {
        if (key != draggedKey) return
        val landing = Animatable(dragPosition.round(), IntOffset.VectorConverter)
        positions[key] = landing
        draggedKey = null
        slots[key]?.let { slot -> scope.launch { landing.animateTo(slot.topLeft, PlacementSpec) } }
        if (moved) onDrop()
    }

    fun forget(key: String) {
        if (key == draggedKey) onDragEnd(key)
        slots.remove(key)
        positions.remove(key)
    }

    // The next swap waits until the dragged tile takes the slot of the previous target
    private fun moveIfCrossed(key: String) {
        val slot = slots[key] ?: return
        awaitedSlot?.let { awaited ->
            if (slot != awaited) return
            awaitedSlot = null
        }
        val center = (dragPosition + Offset(slot.width / 2f, slot.height / 2f)).round()
        val target = slots.entries.firstOrNull { (otherKey, other) ->
            otherKey != key && otherKey != ADD_TILE_KEY && other.contains(center)
        } ?: return
        awaitedSlot = target.value
        moved = true
        onMove(key, target.key)
    }
}

@Composable
private fun rememberAppsDragState(onMove: (String, String) -> Unit, onDrop: () -> Unit): AppsDragState {
    val scope = rememberCoroutineScope()
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDrop by rememberUpdatedState(onDrop)
    return remember {
        AppsDragState(
            scope = scope,
            onMove = { fromKey, toKey -> currentOnMove(fromKey, toKey) },
            onDrop = { currentOnDrop() }
        )
    }
}
