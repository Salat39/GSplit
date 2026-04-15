package com.salat.settings.closingOverlay.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.closingOverlay.presentation.route.SettingsClosingOverlayNavRoute

fun NavGraphBuilder.settingsClosingOverlayScreen(onNavigateBack: () -> Unit) =
    composable<SettingsClosingOverlayNavRoute> {
        // Screen viewModel
        val viewModel: SettingsClosingOverlayViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        // Override system handler
        BackHandler(onBack = onNavigateBack)

        SettingsClosingOverlayScreen(
            state = state,
            sendAction = viewModel::sendAction,
            onNavigateBack = onNavigateBack
        )
    }

fun NavController.navigateToSettingsClosingOverlay(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsClosingOverlayNavRoute, builder ?: {})
