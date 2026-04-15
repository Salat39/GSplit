package com.salat.ui

import android.content.res.Configuration
import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.asIntState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.salat.entity.SystemInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun rememberTimeLockedBoolean(delay: Long = 1000L): MutableState<Boolean> {
    val lock = remember { mutableStateOf(false) }
    LaunchedEffect(lock) {
        snapshotFlow { lock.value }
            .distinctUntilChanged()
            .filter { it }
            .collect { _ ->
                delay(delay)
                lock.value = false
            }
    }
    return lock
}

@Composable
fun rememberDrawerMenuBodyShifting(state: DrawerState, shift: Float = 40f): FloatState {
    val delta = 8f // space before shifting
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val drawerProgress = rememberSaveable { mutableFloatStateOf(0f) }
    var drawerWidth by rememberSaveable { mutableFloatStateOf(0f) }
    SideEffect {
        if (drawerWidth == 0f) {
            drawerWidth = if (state.currentOffset.isNaN()) 0f else state.currentOffset
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.currentOffset }
            .distinctUntilChanged()
            .collect { _ ->
                val width = if (drawerWidth.isNaN()) 0f else drawerWidth
                val drawerOffset = (state.currentOffset - width) / (0f - width)
                val clearOffset = if (drawerOffset.isInfinite()) 0f else drawerOffset
                val calcProgress = ((clearOffset * (shift + delta)) - delta).coerceAtLeast(0f).dp.toPxFloat
                drawerProgress.floatValue = if (isRtl) -calcProgress else calcProgress
            }
    }
    return drawerProgress.asFloatState()
}

/**
 * Tracking keyboard state
 *
 * first - Keyboard visibility flag
 * second - Keyboard height
 */
@Composable
fun rememberKeyboardSpecsState(): Pair<State<Boolean>, IntState> {
    val view = LocalView.current
    val isKeyboardVisible = remember { mutableStateOf(false) }
    val keyboardHeight = remember { mutableIntStateOf(0) }
    val config = LocalConfiguration.current

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardVisible.value = keypadHeight > screenHeight * 0.15
            keyboardHeight.intValue = if (keypadHeight > screenHeight * 0.15) keypadHeight else 0
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    LaunchedEffect(config) {
        isKeyboardVisible.value = false
    }

    return Pair(isKeyboardVisible, keyboardHeight.asIntState())
}

@Composable
fun rememberKeyboardImeVisibilityState(): State<Boolean> {
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val isImeVisible = remember { derivedStateOf { ime.getBottom(density) > 0 } }
    return isImeVisible
}

@Stable
@Composable
fun rememberPainterResource(@DrawableRes id: Int): VectorPainter {
    val imageVector = ImageVector.vectorResource(id)
    return rememberVectorPainter(imageVector)
}

@Composable
fun rememberSystemInsets(): State<SystemInsets> {
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    val configuration = LocalConfiguration.current
    val statusBarHeight by remember(configuration.orientation) {
        derivedStateOf { systemBarsPadding.calculateTopPadding() }
    }
    val navBarHeight by remember(configuration.orientation) {
        derivedStateOf { systemBarsPadding.calculateBottomPadding() }
    }
    val layoutDirection = LocalLayoutDirection.current
    val startSystemBarWidth by remember(configuration.orientation) {
        derivedStateOf { systemBarsPadding.calculateStartPadding(layoutDirection) }
    }
    val endSystemBarWidth by remember(configuration.orientation) {
        derivedStateOf { systemBarsPadding.calculateEndPadding(layoutDirection) }
    }
    return remember {
        derivedStateOf {
            SystemInsets(
                start = startSystemBarWidth,
                top = statusBarHeight,
                end = endSystemBarWidth,
                bottom = navBarHeight
            )
        }
    }
}

@Composable
fun rememberIsLandscape(): Boolean {
    val configuration = LocalConfiguration.current

    return remember(configuration.orientation) {
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}
