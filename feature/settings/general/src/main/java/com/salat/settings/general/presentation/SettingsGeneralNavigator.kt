package com.salat.settings.general.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.general.presentation.route.SettingsGeneralNavRoute

fun NavGraphBuilder.settingsGeneralScreen(
    onNavigateToAutostart: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToUi: () -> Unit,
    onNavigateToAdb: () -> Unit,
    onNavigateToClosingOverlay: () -> Unit,
    onNavigateToAppSwitchOverlay: () -> Unit,
    onNavigateToDarkScreenMode: () -> Unit,
    onNavigateToWindowShiftMode: () -> Unit,
    onNavigateToAppTasks: () -> Unit,
    onNavigateToApi: () -> Unit,
    onNavigateBack: () -> Unit
) = composable<SettingsGeneralNavRoute> {
    // Screen viewModel
    val viewModel: SettingsGeneralViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsGeneralScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateToAutostart = onNavigateToAutostart,
        onNavigateToPresets = onNavigateToPresets,
        onNavigateToUi = onNavigateToUi,
        onNavigateToAdb = onNavigateToAdb,
        onNavigateToClosingOverlay = onNavigateToClosingOverlay,
        onNavigateToAppSwitchOverlay = onNavigateToAppSwitchOverlay,
        onNavigateToDarkScreenMode = onNavigateToDarkScreenMode,
        onNavigateToWindowShiftMode = onNavigateToWindowShiftMode,
        onNavigateToAppTasks = onNavigateToAppTasks,
        onNavigateToApi = onNavigateToApi,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsGeneral(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsGeneralNavRoute, builder ?: {})
