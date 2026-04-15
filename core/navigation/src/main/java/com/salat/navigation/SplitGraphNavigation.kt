package com.salat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navigation
import com.salat.navigation.routs.SplitNavGraph
import com.salat.settings.adb.navigateToSettingsAdb
import com.salat.settings.adb.settingsAdbScreen
import com.salat.settings.add.presentation.navigateToAdd
import com.salat.settings.add.presentation.splitAddScreen
import com.salat.settings.api.presentation.navigateToSettingsApi
import com.salat.settings.api.presentation.settingsApiScreen
import com.salat.settings.appSwitch.presentation.navigateToSettingsAppSwitchOverlay
import com.salat.settings.appSwitch.presentation.settingsAppSwitchOverlayScreen
import com.salat.settings.apptasks.presentation.navigateToSettingsAppTasks
import com.salat.settings.apptasks.presentation.settingsAppTasksScreen
import com.salat.settings.autostart.presentation.navigateToSettingsAutostart
import com.salat.settings.autostart.presentation.settingsAutostartScreen
import com.salat.settings.closingOverlay.presentation.navigateToSettingsClosingOverlay
import com.salat.settings.closingOverlay.presentation.settingsClosingOverlayScreen
import com.salat.settings.darkScreenMode.presentation.navigateToSettingsDarkScreenMode
import com.salat.settings.darkScreenMode.presentation.settingsDarkScreenModeScreen
import com.salat.settings.general.presentation.navigateToSettingsGeneral
import com.salat.settings.general.presentation.settingsGeneralScreen
import com.salat.settings.list.presentation.route.SplitListNavRoute
import com.salat.settings.list.presentation.splitListScreen
import com.salat.settings.presets.presentation.navigateToSettingsPresets
import com.salat.settings.presets.presentation.settingsPresetsScreen
import com.salat.settings.replacementapps.navigateToSettingsReplacementApps
import com.salat.settings.replacementapps.settingsReplacementAppsScreen
import com.salat.settings.scheduler.presentation.navigateToSettingsScheduler
import com.salat.settings.scheduler.presentation.settingsSchedulerScreen
import com.salat.settings.ui.navigateToSettingsUi
import com.salat.settings.ui.settingsUiScreen
import com.salat.settings.windowshiftmode.presentation.navigateToSettingsWindowShiftMode
import com.salat.settings.windowshiftmode.presentation.settingsWindowShiftModeScreen
import com.salat.stub.presentation.stubScreen

fun NavGraphBuilder.splitGraph(toolbarExtraSize: Int, navController: NavController) = navigation<SplitNavGraph>(
    startDestination = SplitListNavRoute(toolbarExtraSize = toolbarExtraSize)
) {
    splitAddScreen(
        onNavigateBack = navController::navigateUp
    )
    splitListScreen(
        onNavigateToAdd = { editId, type -> navController.navigateToAdd(editId, type) },
        onNavigateToSettings = navController::navigateToSettingsGeneral
    )
    stubScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsGeneralScreen(
        onNavigateToAutostart = navController::navigateToSettingsAutostart,
        onNavigateToPresets = navController::navigateToSettingsPresets,
        onNavigateToUi = navController::navigateToSettingsUi,
        onNavigateToAdb = navController::navigateToSettingsAdb,
        onNavigateToClosingOverlay = navController::navigateToSettingsClosingOverlay,
        onNavigateToAppSwitchOverlay = navController::navigateToSettingsAppSwitchOverlay,
        onNavigateToDarkScreenMode = navController::navigateToSettingsDarkScreenMode,
        onNavigateToWindowShiftMode = navController::navigateToSettingsWindowShiftMode,
        onNavigateToAppTasks = navController::navigateToSettingsAppTasks,
        onNavigateToApi = navController::navigateToSettingsApi,
        onNavigateBack = navController::navigateUp
    )
    settingsSchedulerScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsPresetsScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsUiScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsAdbScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsClosingOverlayScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsAppSwitchOverlayScreen(
        onNavigateToReplacementApps = navController::navigateToSettingsReplacementApps,
        onNavigateBack = navController::navigateUp
    )
    settingsDarkScreenModeScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsWindowShiftModeScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsAppTasksScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsReplacementAppsScreen(
        onNavigateBack = navController::navigateUp
    )
    settingsAutostartScreen(
        onNavigateToScheduler = navController::navigateToSettingsScheduler,
        onNavigateBack = navController::navigateUp
    )
    settingsApiScreen(
        onNavigateBack = navController::navigateUp
    )
}

fun NavController.navigateToSplitGraph(builder: (NavOptionsBuilder.() -> Unit)? = null) =
    navigate(SplitNavGraph, builder ?: {})
