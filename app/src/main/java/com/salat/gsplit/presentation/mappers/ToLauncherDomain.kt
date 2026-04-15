package com.salat.gsplit.presentation.mappers

import com.salat.splitlauncher.domain.entity.SplitLaunchApp
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import com.salat.splitlauncher.domain.entity.SplitLaunchType
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset

internal fun SplitPreset.toLauncherDomain() = SplitLaunchTask(
    firstApp = firstApp.toLauncherDomain(),
    type = type.toLauncherDomain(),
    secondApp = secondApp.toLauncherDomain(),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    id = id
)

internal fun AppPreset.toLauncherDomain() = SplitLaunchApp(
    title = title,
    packageName = packageName,
    autoPlay = autoPlay
)

internal fun PresetType.toLauncherDomain() = when (this) {
    PresetType.HALF -> SplitLaunchType.HALF
    PresetType.ONE_TO_THREE -> SplitLaunchType.ONE_TO_THREE
    PresetType.TWO_TO_THREE -> SplitLaunchType.TWO_TO_THREE
    PresetType.THREE_TO_FOUR -> SplitLaunchType.THREE_TO_FOUR
    PresetType.THREE_TO_TWO -> SplitLaunchType.THREE_TO_TWO
    PresetType.FOUR_TO_THREE -> SplitLaunchType.FOUR_TO_THREE
}
