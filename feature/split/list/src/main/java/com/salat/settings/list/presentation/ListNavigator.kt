package com.salat.settings.list.presentation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.salat.settings.list.presentation.route.SplitListNavRoute

fun NavGraphBuilder.splitListScreen(
    onNavigateToAdd: (editId: Long?, type: Int?) -> Unit,
    onNavigateToSettings: () -> Unit
) = composable<SplitListNavRoute> {
    val viewModel: ListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiScaleState = viewModel.uiScaleState.collectAsStateWithLifecycle()
    val toolbarExtraSizeState = viewModel.toolbarExtraSizeState.collectAsStateWithLifecycle()

    ListScreen(
        state = state,
        uiScaleState = uiScaleState,
        toolbarExtraSizeState = toolbarExtraSizeState,
        sendAction = viewModel::sendAction,
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToSettings = onNavigateToSettings
    )
}
