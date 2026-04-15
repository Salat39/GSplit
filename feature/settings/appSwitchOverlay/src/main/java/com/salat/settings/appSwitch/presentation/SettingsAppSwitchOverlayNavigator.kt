package com.salat.settings.appSwitch.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.appSwitch.presentation.route.SettingsAppSwitchOverlayNavRoute

fun NavGraphBuilder.settingsAppSwitchOverlayScreen(
    onNavigateToReplacementApps: () -> Unit,
    onNavigateBack: () -> Unit
) = composable<SettingsAppSwitchOverlayNavRoute> {
    // Screen viewModel
    val viewModel: SettingsAppSwitchOverlayViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsAppSwitchOverlayScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateToReplacementApps = onNavigateToReplacementApps,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsAppSwitchOverlay(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsAppSwitchOverlayNavRoute, builder ?: {})
