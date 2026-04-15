package com.salat.settings.scheduler.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.scheduler.presentation.route.SettingsSchedulerNavRoute

fun NavGraphBuilder.settingsSchedulerScreen(onNavigateBack: () -> Unit) = composable<SettingsSchedulerNavRoute> {
    // Screen viewModel
    val viewModel: SettingsSchedulerViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uiScaleState = viewModel.uiScaleState.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsSchedulerScreen(
        state = state,
        uiScaleState = uiScaleState,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsScheduler(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsSchedulerNavRoute, builder ?: {})
