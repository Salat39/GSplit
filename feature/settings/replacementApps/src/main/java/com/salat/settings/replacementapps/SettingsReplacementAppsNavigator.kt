package com.salat.settings.replacementapps

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.replacementapps.route.SettingsReplacementAppsNavRoute

fun NavGraphBuilder.settingsReplacementAppsScreen(onNavigateBack: () -> Unit) =
    composable<SettingsReplacementAppsNavRoute> {
        // Screen viewModel
        val viewModel: SettingsReplacementAppsViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val uiScaleState = viewModel.uiScaleState.collectAsStateWithLifecycle()

        // Override system handler
        BackHandler(onBack = onNavigateBack)

        SettingsReplacementAppsScreen(
            state = state,
            uiScaleState = uiScaleState,
            sendAction = viewModel::sendAction,
            onNavigateBack = onNavigateBack
        )
    }

fun NavController.navigateToSettingsReplacementApps(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsReplacementAppsNavRoute, builder ?: {})
