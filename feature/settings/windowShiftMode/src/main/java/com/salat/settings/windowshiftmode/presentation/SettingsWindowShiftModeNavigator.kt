package com.salat.settings.windowshiftmode.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.windowshiftmode.presentation.route.SettingsWindowShiftModeNavRoute

fun NavGraphBuilder.settingsWindowShiftModeScreen(onNavigateBack: () -> Unit) =
    composable<SettingsWindowShiftModeNavRoute> {
        // Screen viewModel
        val viewModel: SettingsWindowShiftModeViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        // Override system handler
        BackHandler(onBack = onNavigateBack)

        SettingsWindowShiftModeScreen(
            state = state,
            sendAction = viewModel::sendAction,
            onNavigateBack = onNavigateBack
        )
    }

fun NavController.navigateToSettingsWindowShiftMode(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsWindowShiftModeNavRoute, builder ?: {})
