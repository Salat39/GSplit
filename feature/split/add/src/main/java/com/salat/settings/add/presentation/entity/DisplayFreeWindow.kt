package com.salat.settings.add.presentation.entity

import androidx.compose.runtime.Immutable
import kotlin.math.abs

// Bounds are fractions of the free screen area between the status bar and the navigation bar
@Immutable
data class DisplayFreeWindow(
    val id: Int,
    val app: DeviceAppInfo,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val alwaysOnTop: Boolean
) {
    fun moveBy(dx: Float, dy: Float, limits: FreeWindowLimits): DisplayFreeWindow {
        val width = right - left
        val height = bottom - top
        val newLeft = (left + dx)
            .let { it + limits.xLines.snapShift(it, it + width, limits.xSnap) }
            .coerceIn(0f, (1f - width).coerceAtLeast(0f))
        val newTop = (top + dy)
            .let { it + limits.yLines.snapShift(it, it + height, limits.ySnap) }
            .coerceIn(0f, (1f - height).coerceAtLeast(0f))
        return copy(left = newLeft, top = newTop, right = newLeft + width, bottom = newTop + height)
    }

    fun resize(edge: FreeWindowEdge, delta: Float, limits: FreeWindowLimits) = when (edge) {
        FreeWindowEdge.LEFT -> copy(
            left = (left + delta)
                .snapTo(limits.xLines, limits.xSnap)
                .coerceIn(0f, (right - limits.minWidth).coerceAtLeast(0f))
        )

        FreeWindowEdge.TOP -> copy(
            top = (top + delta)
                .snapTo(limits.yLines, limits.ySnap)
                .coerceIn(0f, (bottom - limits.minHeight).coerceAtLeast(0f))
        )

        FreeWindowEdge.RIGHT -> copy(
            right = (right + delta)
                .snapTo(limits.xLines, limits.xSnap)
                .coerceIn((left + limits.minWidth).coerceAtMost(1f), 1f)
        )

        FreeWindowEdge.BOTTOM -> copy(
            bottom = (bottom + delta)
                .snapTo(limits.yLines, limits.ySnap)
                .coerceIn((top + limits.minHeight).coerceAtMost(1f), 1f)
        )
    }
}

private fun Float.snapTo(lines: List<Float>, snap: Float): Float {
    val nearest = lines.minByOrNull { abs(it - this) } ?: return this
    return if (abs(nearest - this) <= snap) nearest else this
}

private fun List<Float>.snapShift(start: Float, end: Float, snap: Float): Float {
    val shift = flatMap { listOf(it - start, it - end) }.minByOrNull { abs(it) } ?: return 0f
    return if (abs(shift) <= snap) shift else 0f
}
