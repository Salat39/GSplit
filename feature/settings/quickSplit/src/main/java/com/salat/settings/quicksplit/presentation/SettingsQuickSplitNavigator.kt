package com.salat.settings.quicksplit.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.quicksplit.presentation.route.SettingsQuickSplitNavRoute

fun NavGraphBuilder.settingsQuickSplitScreen(onNavigateToDefaultRatio: () -> Unit, onNavigateBack: () -> Unit) =
    composable<SettingsQuickSplitNavRoute> {
        val viewModel: SettingsQuickSplitViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        val uiScaleState = viewModel.uiScaleState.collectAsStateWithLifecycle()

        BackHandler(onBack = onNavigateBack)

        SettingsQuickSplitScreen(
            state = state,
            uiScaleState = uiScaleState,
            sendAction = viewModel::sendAction,
            onNavigateToDefaultRatio = onNavigateToDefaultRatio,
            onNavigateBack = onNavigateBack
        )
    }

fun NavController.navigateToSettingsQuickSplit(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsQuickSplitNavRoute, builder ?: {})
