package com.salat.gsplit.presentation.components

import com.salat.firebase.domain.entity.AnalyticsScreen
import com.salat.settings.add.presentation.route.SPLIT_ADD_NAV_ROUTE_NAME
import com.salat.settings.autostart.presentation.route.SETTINGS_AUTOSTART_NAV_ROUTE_NAME
import com.salat.settings.general.presentation.route.SETTINGS_GENERAL_NAV_ROUTE_NAME
import com.salat.settings.list.presentation.route.SPLIT_LIST_NAV_ROUTE_NAME
import com.salat.settings.presets.presentation.route.SETTINGS_PRESETS_NAV_ROUTE_NAME
import com.salat.settings.scheduler.presentation.route.SETTINGS_SCHEDULER_NAV_ROUTE_NAME

internal fun String.toAnalyticsRoute() = when (this) {
    SPLIT_LIST_NAV_ROUTE_NAME -> AnalyticsScreen.PRESETS_LIST
    SPLIT_ADD_NAV_ROUTE_NAME -> AnalyticsScreen.ADD
    SETTINGS_GENERAL_NAV_ROUTE_NAME -> AnalyticsScreen.SETTINGS_GENERAL
    SETTINGS_SCHEDULER_NAV_ROUTE_NAME -> AnalyticsScreen.SETTINGS_SCHEDULER
    SETTINGS_AUTOSTART_NAV_ROUTE_NAME -> AnalyticsScreen.SETTINGS_AUTOSTART
    SETTINGS_PRESETS_NAV_ROUTE_NAME -> AnalyticsScreen.SETTINGS_PRESETS
    else -> ""
}
