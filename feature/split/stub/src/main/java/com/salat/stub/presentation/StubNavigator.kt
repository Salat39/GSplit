package com.salat.stub.presentation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.salat.stub.presentation.route.StubNavRoute

fun NavGraphBuilder.stubScreen(onNavigateBack: () -> Unit) = composable<StubNavRoute> {
    val viewModel: StubViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    StubScreen(
        state = state,
        onNavigateToBack = onNavigateBack
    )
}

fun NavController.navigateToStub(
    minimizeAfterCloseScreen: Boolean,
    toolbarExtraSize: Int,
    builder: (NavOptionsBuilder.() -> Unit)? = null
) = navigate(StubNavRoute(minimizeAfterCloseScreen, toolbarExtraSize), builder ?: {})
