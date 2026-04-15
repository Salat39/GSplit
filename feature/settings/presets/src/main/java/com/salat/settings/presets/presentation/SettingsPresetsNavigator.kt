package com.salat.settings.presets.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.presets.presentation.route.SettingsPresetsNavRoute

fun NavGraphBuilder.settingsPresetsScreen(onNavigateBack: () -> Unit) = composable<SettingsPresetsNavRoute> {
    // Screen viewModel
    val viewModel: SettingsPresetsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsPresetsScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsPresets(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsPresetsNavRoute, builder ?: {})
