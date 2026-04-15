package com.salat.settings.darkScreenMode.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.darkScreenMode.presentation.route.SettingsDarkScreenModeNavRoute

fun NavGraphBuilder.settingsDarkScreenModeScreen(onNavigateBack: () -> Unit) =
    composable<SettingsDarkScreenModeNavRoute> {
        // Screen viewModel
        val viewModel: SettingsDarkScreenModeViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()

        // Override system handler
        BackHandler(onBack = onNavigateBack)

        SettingsDarkScreenModeScreen(
            state = state,
            sendAction = viewModel::sendAction,
            onNavigateBack = onNavigateBack
        )
    }

fun NavController.navigateToSettingsDarkScreenMode(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsDarkScreenModeNavRoute, builder ?: {})
