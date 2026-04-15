package com.salat.settings.list.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.salat.settings.list.presentation.components.RenderList
import com.salat.settings.list.presentation.components.RenderNeedDevMode
import com.salat.settings.list.presentation.components.RenderNeedDrawOverlay
import com.salat.settings.list.presentation.components.RenderNeedFreeform
import com.salat.uikit.preview.PreviewScreen
import presentation.getActivity
import presentation.isCanDrawOverlays
import presentation.isDeveloperModeEnabled
import presentation.isFreeformModeEnabled

@Composable
internal fun ListScreen(
    state: ListViewModel.ViewState,
    sendAction: (ListViewModel.Action) -> Unit = {},
    uiScaleState: State<Float>? = null,
    toolbarExtraSizeState: State<Int>? = null,
    onNavigateToAdd: (editId: Long?, type: Int?) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {}
) = Scaffold { innerPadding ->
    val context = LocalContext.current
    BackHandler { context.getActivity()?.moveTaskToBack(true) }

    // Lifecycle events
    var resumeCounter by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    resumeCounter += 1
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isNeedDevMode = remember(resumeCounter) { !context.isDeveloperModeEnabled() }
    val isNeedFreedom = remember(resumeCounter) { !context.isFreeformModeEnabled() }
    val isNeedDrawOverlay = remember(resumeCounter) { !context.isCanDrawOverlays() }

    when {
        isNeedDevMode -> RenderNeedDevMode(innerPadding)

        isNeedFreedom -> RenderNeedFreeform(innerPadding)

        isNeedDrawOverlay -> RenderNeedDrawOverlay(innerPadding)

        else -> RenderList(
            state,
            sendAction,
            uiScaleState,
            toolbarExtraSizeState,
            onNavigateToAdd,
            onNavigateToSettings,
            innerPadding
        )
    }
}

@Preview
@Composable
private fun ListScreenDataPreview() {
    PreviewScreen {
        ListScreen(
            state = ListViewModel.ViewState()
        )
    }
}
