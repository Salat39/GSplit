package com.salat.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.salat.navigation.routs.NoUiNavGraph
import com.salat.stub.presentation.route.StubNavRoute
import com.salat.stub.presentation.stubScreen

fun NavGraphBuilder.noUiGraph(toolbarExtraSize: Int, navBack: () -> Unit) = navigation<NoUiNavGraph>(
    startDestination = StubNavRoute(false, toolbarExtraSize)
) {
    stubScreen(
        onNavigateBack = navBack
    )
}
