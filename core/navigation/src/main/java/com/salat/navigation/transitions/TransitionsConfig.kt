package com.salat.navigation.transitions

import com.salat.navigation.transitions.entity.TransitionKey
import com.salat.navigation.transitions.entity.TransitionRule
import com.salat.navigation.transitions.entity.TransitionType
import com.salat.settings.add.presentation.route.SPLIT_ADD_NAV_ROUTE_NAME
import com.salat.settings.api.presentation.route.SETTINGS_API_NAV_ROUTE_NAME
import com.salat.settings.appSwitch.presentation.route.SETTINGS_APP_SWITCH_OVERLAY_NAV_ROUTE_NAME
import com.salat.settings.apptasks.presentation.route.SETTINGS_APP_TASKS_NAV_ROUTE_NAME
import com.salat.settings.autostart.presentation.route.SETTINGS_AUTOSTART_NAV_ROUTE_NAME
import com.salat.settings.closingOverlay.presentation.route.SETTINGS_CLOSING_OVERLAY_NAV_ROUTE_NAME
import com.salat.settings.darkScreenMode.presentation.route.SETTINGS_DARK_SCREEN_MODE_NAV_ROUTE_NAME
import com.salat.settings.general.presentation.route.SETTINGS_GENERAL_NAV_ROUTE_NAME
import com.salat.settings.list.presentation.route.SPLIT_LIST_NAV_ROUTE_NAME
import com.salat.settings.presets.presentation.route.SETTINGS_PRESETS_NAV_ROUTE_NAME
import com.salat.settings.replacementapps.route.SETTINGS_REPLACEMENT_APPS_NAV_ROUTE_NAME
import com.salat.settings.scheduler.presentation.route.SETTINGS_SCHEDULER_NAV_ROUTE_NAME
import com.salat.settings.ui.route.SETTINGS_UI_NAV_ROUTE_NAME
import com.salat.settings.windowshiftmode.presentation.route.SETTINGS_WINDOW_SHIFT_MODE_NAV_ROUTE_NAME
import com.salat.stub.presentation.route.STUB_NAV_ROUTE_NAME

/**
 * A set of data that defines animation transitions between screens
 */
val transitionsMap: Map<TransitionKey, TransitionRule> = listOf(
    TransitionRule(
        enter = SPLIT_LIST_NAV_ROUTE_NAME,
        exit = SPLIT_ADD_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SPLIT_LIST_NAV_ROUTE_NAME,
        exit = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_AUTOSTART_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_AUTOSTART_NAV_ROUTE_NAME,
        exit = SETTINGS_SCHEDULER_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_APP_SWITCH_OVERLAY_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_APP_SWITCH_OVERLAY_NAV_ROUTE_NAME,
        exit = SETTINGS_REPLACEMENT_APPS_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_APP_TASKS_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_CLOSING_OVERLAY_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_DARK_SCREEN_MODE_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_PRESETS_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_UI_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_WINDOW_SHIFT_MODE_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SETTINGS_GENERAL_NAV_ROUTE_NAME,
        exit = SETTINGS_API_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.DELAYED_SLIDE
    ),
    TransitionRule(
        enter = SPLIT_LIST_NAV_ROUTE_NAME,
        exit = STUB_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.FADE
    ),
    TransitionRule(
        enter = STUB_NAV_ROUTE_NAME,
        exit = SPLIT_LIST_NAV_ROUTE_NAME,
        invert = false,
        type = TransitionType.FADE
    )
).associateBy { rule ->
    TransitionKey(rule.enter, rule.exit, rule.graph)
}
