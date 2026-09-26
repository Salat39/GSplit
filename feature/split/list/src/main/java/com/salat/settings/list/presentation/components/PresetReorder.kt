package com.salat.settings.list.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.ui.rememberPainterResource
import com.salat.uikit.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val AutoScrollEdge = 48.dp
private val AutoScrollMaxStep = 14.dp

@Stable
internal class PresetDragState(
    private val listState: LazyListState,
    private val scope: CoroutineScope,
    private val onMove: (fromId: Long, toId: Long) -> Unit,
    private val onDrop: () -> Unit
) {
    var draggedId by mutableStateOf<Long?>(null)
        private set

    var landingId by mutableStateOf<Long?>(null)
        private set

    var landingOffset by mutableFloatStateOf(0f)
        private set

    private var startOffset by mutableFloatStateOf(0f)
    private var dragDelta by mutableFloatStateOf(0f)
    private var awaitedIndex: Int? = null
    private var moved = false
    private var landingJob: Job? = null

    val draggedOffset: Float
        get() = draggedItem()?.let { startOffset + dragDelta - it.offset } ?: 0f

    fun onDragStart(id: Long): Boolean {
        if (draggedId != null) return false
        val item = itemInfo(id) ?: return false
        val carriedOffset = if (landingId == id) {
            landingJob?.cancel()
            landingId = null
            landingOffset
        } else 0f
        startOffset = item.offset + carriedOffset
        dragDelta = 0f
        awaitedIndex = null
        moved = false
        draggedId = id
        return true
    }

    fun onDrag(id: Long, delta: Float) {
        if (id != draggedId) return
        dragDelta += delta
        moveIfCrossed()
    }

    fun onDragStop(id: Long) {
        if (id != draggedId) return
        landingJob?.cancel()
        landingOffset = draggedOffset
        landingId = id
        draggedId = null
        if (moved) onDrop()
        landingJob = scope.launch {
            animate(landingOffset, 0f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { value, _ ->
                landingOffset = value
            }
            landingId = null
        }
    }

    fun autoScrollStep(edge: Float, maxStep: Float): Float {
        val dragged = draggedItem() ?: return 0f
        val layoutInfo = listState.layoutInfo
        val top = startOffset + dragDelta
        val overTop = layoutInfo.viewportStartOffset + edge - top
        val overBottom = top + dragged.size - (layoutInfo.viewportEndOffset - edge)
        return when {
            overTop > 0f -> -maxStep * (overTop / edge).coerceAtMost(1f)
            overBottom > 0f -> maxStep * (overBottom / edge).coerceAtMost(1f)
            else -> 0f
        }
    }

    // Swap only after the dragged center passes the neighbor center
    // This prevents a repeated swap of cards with different height
    fun moveIfCrossed() {
        val dragged = draggedItem() ?: return
        val awaited = awaitedIndex
        if (awaited != null && dragged.index != awaited) return
        awaitedIndex = null

        val center = startOffset + dragDelta + dragged.size / 2f
        val presets = listState.layoutInfo.visibleItemsInfo.filter { it.key is Long && it.key != dragged.key }
        val target = presets.lastOrNull { it.index > dragged.index && center > it.center }
            ?: presets.firstOrNull { it.index < dragged.index && center < it.center }
            ?: return

        // Keep the scroll position when the first visible item moves
        val firstIndex = listState.firstVisibleItemIndex
        if (dragged.index == firstIndex || target.index == firstIndex) {
            listState.requestScrollToItem(firstIndex, listState.firstVisibleItemScrollOffset)
        }
        awaitedIndex = target.index
        moved = true
        onMove(dragged.key as Long, target.key as Long)
    }

    private val LazyListItemInfo.center get() = offset + size / 2f

    private fun draggedItem() = draggedId?.let(::itemInfo)

    private fun itemInfo(id: Long) = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id }
}

@Composable
internal fun rememberPresetDragState(
    listState: LazyListState,
    onMove: (fromId: Long, toId: Long) -> Unit,
    onDrop: () -> Unit
): PresetDragState {
    val scope = rememberCoroutineScope()
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnDrop by rememberUpdatedState(onDrop)
    val state = remember(listState) {
        PresetDragState(
            listState = listState,
            scope = scope,
            onMove = { fromId, toId -> currentOnMove(fromId, toId) },
            onDrop = { currentOnDrop() }
        )
    }

    val density = LocalDensity.current
    val isDragging = state.draggedId != null
    LaunchedEffect(isDragging) {
        val edge = with(density) { AutoScrollEdge.toPx() }
        val maxStep = with(density) { AutoScrollMaxStep.toPx() }
        while (state.draggedId != null) {
            withFrameNanos { }
            val step = state.autoScrollStep(edge, maxStep)
            if (step != 0f) {
                listState.scrollBy(step)
                state.moveIfCrossed()
            }
        }
    }
    return state
}

internal fun Modifier.presetDragHandle(state: PresetDragState, id: Long, onDragStart: () -> Unit) =
    pointerInput(state, id) {
        try {
            detectDragGestures(
                onDragStart = { if (state.onDragStart(id)) onDragStart() },
                onDragEnd = { state.onDragStop(id) },
                onDragCancel = { state.onDragStop(id) },
                onDrag = { change, dragAmount ->
                    change.consume()
                    state.onDrag(id, dragAmount.y)
                }
            )
        } finally {
            // The card can leave the composition during the drag
            state.onDragStop(id)
        }
    }

@Composable
internal fun RenderReorderHint(modifier: Modifier) = Row(
    modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 5.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(AppTheme.colors.contentAccent.copy(alpha = .14f))
        .padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
) {
    Icon(
        painter = rememberPainterResource(R.drawable.ic_drag_grip),
        contentDescription = null,
        tint = AppTheme.colors.contentPrimary.copy(.7f),
        modifier = Modifier.size(18.dp)
    )
    Text(
        text = stringResource(R.string.reorder_hint),
        style = AppTheme.typography.aboutText,
        color = AppTheme.colors.contentPrimary.copy(.85f)
    )
}
