package com.salat.settings.autostart.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.autostart.presentation.route.SettingsAutostartNavRoute

fun NavGraphBuilder.settingsAutostartScreen(onNavigateToScheduler: () -> Unit, onNavigateBack: () -> Unit) =
    composable<SettingsAutostartNavRoute> {
        // Screen viewModel
        val viewModel: SettingsAutostartViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        // Override system handler
        BackHandler(onBack = onNavigateBack)

        SettingsAutostartScreen(
            state = state,
            sendAction = viewModel::sendAction,
            onNavigateToScheduler = onNavigateToScheduler,
            onNavigateBack = onNavigateBack
        )
    }

fun NavController.navigateToSettingsAutostart(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsAutostartNavRoute, builder ?: {})
