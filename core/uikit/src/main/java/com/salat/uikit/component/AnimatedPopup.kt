package com.salat.uikit.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun AnimatedPopup(
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset(0, 0),
    uiScaleState: State<Float>?,
    properties: PopupProperties = PopupProperties(),
    enter: EnterTransition = fadeIn(tween(140)),
    exit: ExitTransition = fadeOut(tween(140)),
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val visibleState = rememberSaveable { mutableStateOf(true) }

    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = visibleState.value

    // Listen for state changes and call onDismiss when exit animation completes
    LaunchedEffect(expandedState) {
        snapshotFlow { expandedState.isIdle && !expandedState.targetState }
            .collect { isExitComplete ->
                if (isExitComplete) {
                    onDismissRequest?.let { it() }
                }
            }
    }

    if (expandedState.currentState || expandedState.targetState || !expandedState.isIdle) {
        Popup(
            alignment = alignment,
            offset = offset,
            onDismissRequest = {
                visibleState.value = false
            },
            properties = properties
        ) {
            val density = LocalDensity.current
            val scaledDensity = remember(density, uiScaleState?.value ?: 1f) {
                Density(
                    density.density * (uiScaleState?.value ?: 1f),
                    density.fontScale * (uiScaleState?.value ?: 1f)
                )
            }

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                AnimatedVisibility(
                    visibleState = expandedState,
                    enter = enter,
                    exit = exit,
                    content = content
                )
            }
        }
    }
}
