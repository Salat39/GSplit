package com.salat.gsplit.presentation.mappers

import com.salat.splitlauncher.domain.entity.SplitLaunchApp
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import com.salat.splitlauncher.domain.entity.SplitLaunchType
import com.salat.splitlauncher.domain.entity.SplitLaunchWindow
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.FreeWindowPreset
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset

internal fun SplitPreset.toLauncherDomain() = SplitLaunchTask(
    firstApp = firstApp.toLauncherDomain(),
    type = type.toLauncherDomain(),
    secondApp = secondApp.toLauncherDomain(),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    id = id,
    windows = windows.map { it.toLauncherDomain() },
    ratio = ratio
)

internal fun FreeWindowPreset.toLauncherDomain() = SplitLaunchWindow(
    app = app.toLauncherDomain(),
    left = left,
    top = top,
    right = right,
    bottom = bottom,
    alwaysOnTop = alwaysOnTop
)

internal fun AppPreset.toLauncherDomain() = SplitLaunchApp(
    title = title,
    packageName = packageName,
    autoPlay = autoPlay,
    withCaption = withCaption,
    mainWindow = mainWindow
)

internal fun PresetType.toLauncherDomain() = when (this) {
    PresetType.HALF -> SplitLaunchType.HALF
    PresetType.ONE_TO_THREE -> SplitLaunchType.ONE_TO_THREE
    PresetType.TWO_TO_THREE -> SplitLaunchType.TWO_TO_THREE
    PresetType.THREE_TO_FOUR -> SplitLaunchType.THREE_TO_FOUR
    PresetType.THREE_TO_TWO -> SplitLaunchType.THREE_TO_TWO
    PresetType.FOUR_TO_THREE -> SplitLaunchType.FOUR_TO_THREE
    PresetType.FREE -> SplitLaunchType.FREE
    PresetType.CUSTOM -> SplitLaunchType.CUSTOM
}
