package com.salat.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope

suspend fun PointerInputScope.routePointerChangesTo(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {},
    blockOriginalEvent: Boolean = false
) {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            event.changes.forEach {
                when (event.type) {
                    PointerEventType.Press -> onDown(it)
                    PointerEventType.Move -> onMove(it)
                    PointerEventType.Release -> onUp(it)
                }
                if (blockOriginalEvent) {
                    it.consume()
                }
            }
        } while (event.changes.any { it.pressed })
    }
}
