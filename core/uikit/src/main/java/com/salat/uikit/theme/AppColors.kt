package com.salat.uikit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val isDark: Boolean,

    val surfaceLayerAccentPale: Color,
    val surfaceBackground: Color,
    val surfaceLayer1: Color,
    val surfaceMenu: Color,
    val surfaceMenuDivider: Color,
    val surfaceSettings: Color,
    val surfaceSettingsLayer1: Color,
    val cardItemBackground: Color,
    val contentAccent: Color,
    val contentLightAccent: Color,
    val settingsTitleAccent: Color,
    val quickLaunchAccent: Color,
    val deleteButton: Color,
    val contentPrimary: Color,
    val contentWarning: Color,
    val warning: Color,
    val addSplitTop: Color,
    val addSplitBottom: Color,
    val menuIcon: Color,
    val sliderPassive: Color,
    val autoStart: Color,
    val historyBorder: Color,
    val historyAccentBorder: Color,
    val statusSuccess: Color,
    val statusError: Color,
    val statusDisabled: Color,
    val statusWarning: Color,
)
