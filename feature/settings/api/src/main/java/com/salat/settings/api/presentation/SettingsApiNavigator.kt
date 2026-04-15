package com.salat.settings.api.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.api.presentation.route.SettingsApiNavRoute

fun NavGraphBuilder.settingsApiScreen(onNavigateBack: () -> Unit) = composable<SettingsApiNavRoute> {
    // Screen viewModel
    val viewModel: SettingsApiViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsApiScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsApi(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsApiNavRoute, builder ?: {})
