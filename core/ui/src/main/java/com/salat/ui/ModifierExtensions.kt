package com.salat.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.gradientBackground(colors: List<Color>, angle: Float) = this.then(
    Modifier.drawBehind {
        val angleRad = angle / 180f * PI
        val x = cos(angleRad).toFloat() // Fractional x
        val y = sin(angleRad).toFloat() // Fractional y

        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f
        val offset = center + Offset(x * radius, y * radius)

        val exactOffset = Offset(
            x = min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - min(offset.y.coerceAtLeast(0f), size.height)
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
            size = size
        )
    }
)

fun Modifier.edgeToEdgeImePadding() = composed {
    var consumePadding by remember { mutableIntStateOf(0) }
    onGloballyPositioned { coordinates ->
        val rootCoordinate = coordinates.findRootCoordinates()
        val bottom = coordinates.positionInWindow().y + coordinates.size.height
        consumePadding = (rootCoordinate.size.height - bottom).toInt()
    }
        .consumeWindowInsets(PaddingValues(bottom = consumePadding.toDp))
        .imePadding()
}

// fun Modifier.compatEdgeToEdgeImePadding() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//    edgeToEdgeImePadding()
// } else {
//    imePadding()
// }

fun Modifier.clickableNoRipple(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
) = composed {
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick
    )
}

@Stable
fun Modifier.mirror(): Modifier = composed {
    if (LocalLayoutDirection.current == LayoutDirection.Rtl)
        this.scale(scaleX = -1f, scaleY = 1f)
    else
        this
}

fun Modifier.shimmerEffect(firstColor: Color, secondColor: Color): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ),
        label = "shimmer"
    )
    val colors = remember(firstColor, secondColor) { listOf(firstColor, secondColor, firstColor) }

    drawBehind {
        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
            )
        )
    }.onGloballyPositioned {
        size = it.size
    }
}

fun Modifier.conditional(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier = if (condition) {
    then(modifier(Modifier))
} else this

fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: (Modifier.() -> Modifier)? = null
) = if (condition) {
    then(ifTrue(Modifier))
} else if (ifFalse != null) {
    then(ifFalse(Modifier))
} else {
    this
}

fun Modifier.longPressGesture(
    delayMillis: Long = 1000L,
    rippleEnabled: Boolean = false,
    onLongPress: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val rippleIndication = if (rippleEnabled) rememberRipple() else null

    this
        .indication(interactionSource, rippleIndication)
        .pointerInput(Unit) {
            coroutineScope {
                while (true) {
                    awaitPointerEventScope {
                        val event = awaitPointerEvent()
                        val downChange = event.changes.firstOrNull()

                        if (downChange?.pressed == true) {
                            val pressInteraction = if (rippleEnabled) {
                                PressInteraction.Press(downChange.position)
                            } else null
                            pressInteraction?.let { interactionSource.tryEmit(it) }

                            @Suppress("KotlinConstantConditions")
                            val job = launch {
                                delay(delayMillis)
                                // Check if the pointer is still pressed after the delay
                                if (downChange.pressed) {
                                    onLongPress()
                                }
                            }

                            // Monitor touch release or movement
                            while (true) {
                                val subsequentEvent = awaitPointerEvent()
                                val subsequentChange = subsequentEvent.changes.firstOrNull()

                                if (subsequentChange == null || !subsequentChange.pressed) {
                                    job.cancel() // Cancel the job if the press is released
                                    break
                                }
                            }

                            pressInteraction?.let {
                                interactionSource.tryEmit(PressInteraction.Release(it))
                            }
                        }
                    }
                }
            }
        }
}

@Stable
fun Modifier.leftLineBackground(leftLineColor: Color, containerColor: Color, lineSize: Dp): Modifier = this.then(
    Modifier.drawBehind {
        drawLeftLineBackground(leftLineColor, containerColor, lineSize.toPx(), layoutDirection)
    }
)

@Stable
private fun DrawScope.drawLeftLineBackground(
    leftLineColor: Color,
    containerColor: Color,
    lineSizePx: Float,
    layoutDirection: LayoutDirection
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val offsetX = if (isRtl) size.width - lineSizePx else 0f

    drawRect(
        color = leftLineColor,
        topLeft = Offset(offsetX, 0f),
        size = Size(lineSizePx, size.height)
    )

    val backgroundOffsetX = if (isRtl) 0f else lineSizePx
    drawRect(
        color = containerColor,
        topLeft = Offset(backgroundOffsetX, 0f),
        size = Size(size.width - lineSizePx, size.height)
    )
}

@Suppress("unused")
fun Modifier.logMeasureHeight(): Modifier = composed {
    val localDensity = LocalDensity.current
    this.onGloballyPositioned { coordinates: LayoutCoordinates ->
        // Calculate height in pixels and density-independent pixels (dp)
        val heightInPx = coordinates.size.height.toFloat()
        val heightInDp = with(localDensity) { coordinates.size.height.toDp() }
        // Log the height values
        println("Element measurement result: $heightInPx pixels or 15 $heightInDp")
    }
}

@Suppress("unused")
fun Modifier.combinedClickableWithOffset(
    enabled: Boolean = true,
    onClick: (Offset) -> Unit,
    onLongClick: (Offset) -> Unit,
    rippleDelay: Long = 100L
): Modifier = composed {
    if (enabled) {
        val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
        val indication: Indication = LocalIndication.current

        this
            .indication(interactionSource, indication)
            .pointerInput(onClick) {
                var inDelay = false
                detectTapGestures(
                    onPress = { offset ->
                        // Handle the ripple effect with delay to avoid scroll interactions
                        coroutineScope {
                            val pressInteraction = PressInteraction.Press(offset)
                            inDelay = true
                            val rippleJob = launch {
                                delay(rippleDelay) // Wait before showing ripple
                                inDelay = false
                                interactionSource.emit(pressInteraction)
                            }
                            try {
                                if (tryAwaitRelease()) {
                                    // On successful tap release, invoke click and release ripple
                                    rippleJob.cancel()
                                    interactionSource.emit(PressInteraction.Release(pressInteraction))
                                } else {
                                    // If canceled by a scroll, cancel the ripple
                                    rippleJob.cancel()
                                    interactionSource.emit(PressInteraction.Cancel(pressInteraction))
                                }
                            } catch (_: Exception) {
                                // Handle cancellation exceptions
                                rippleJob.cancel()
                                interactionSource.emit(PressInteraction.Cancel(pressInteraction))
                            }
                        }
                    },
                    onLongPress = { offset ->
                        onLongClick(offset)
                    },
                    onTap = { offset ->
                        if (inDelay) {
                            val pressInteraction = PressInteraction.Press(offset)
                            interactionSource.tryEmit(pressInteraction)
                            interactionSource.tryEmit(PressInteraction.Release(pressInteraction))
                        }
                        onClick(offset)
                    }
                )
            }
    } else {
        this
    }
}
