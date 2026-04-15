package com.salat.settings.adb

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.settings.adb.route.SettingsAdbNavRoute

fun NavGraphBuilder.settingsAdbScreen(onNavigateBack: () -> Unit) = composable<SettingsAdbNavRoute> {
    // Screen viewModel
    val viewModel: SettingsAdbViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Override system handler
    BackHandler(onBack = onNavigateBack)

    SettingsAdbScreen(
        state = state,
        sendAction = viewModel::sendAction,
        onNavigateBack = onNavigateBack
    )
}

fun NavController.navigateToSettingsAdb(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SettingsAdbNavRoute, builder ?: {})
