package com.salat.settings.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.ui.route.SettingsUiNavRoute

fun NavGraphBuilder.settingsUiScreen(onNavigateBack: () -> Unit) = composable<SettingsUiNavRoute> {
    // Screen viewModel
    val viewModel: SettingsUiViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsUiScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsUi(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsUiNavRoute, builder ?: {})
