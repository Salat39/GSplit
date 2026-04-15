package com.salat.settings.apptasks.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.apptasks.presentation.route.SettingsAppTasksNavRoute

fun NavGraphBuilder.settingsAppTasksScreen(onNavigateBack: () -> Unit) = composable<SettingsAppTasksNavRoute> {
    // Screen viewModel
    val viewModel: SettingsAppTasksViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsAppTasksScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsAppTasks(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsAppTasksNavRoute, builder ?: {})
