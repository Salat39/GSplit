package com.salat.settings.add.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.add.presentation.route.SplitAddNavRoute

fun NavGraphBuilder.splitAddScreen(onNavigateBack: () -> Unit) = composable<SplitAddNavRoute> {
    // Screen viewModel
    val viewModel: AddViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiScaleState = viewModel.uiScaleState.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    AddScreen(
        state = state,
        uiScaleState = uiScaleState,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToAdd(editId: Long?, type: Int?, builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SplitAddNavRoute(editId, type), builder ?: {})
