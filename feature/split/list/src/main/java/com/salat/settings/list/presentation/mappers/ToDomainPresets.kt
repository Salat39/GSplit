package com.salat.settings.list.presentation.mappers

import com.salat.settings.list.presentation.entity.DisplayAppPreset
import com.salat.settings.list.presentation.entity.DisplayPresetType
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.splitlauncher.domain.entity.SplitLaunchApp
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import com.salat.splitlauncher.domain.entity.SplitLaunchType

internal fun DisplaySplitPreset.toDomain() = SplitLaunchTask(
    firstApp = firstApp.toDomain(),
    type = type.toDomain(),
    secondApp = secondApp.toDomain(),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    id = id
)

internal fun DisplayAppPreset.toDomain() = SplitLaunchApp(
    title = title,
    packageName = packageName,
    autoPlay = autoPlay
)

internal fun DisplayPresetType.toDomain() = when (this) {
    DisplayPresetType.HALF -> SplitLaunchType.HALF
    DisplayPresetType.ONE_TO_THREE -> SplitLaunchType.ONE_TO_THREE
    DisplayPresetType.TWO_TO_THREE -> SplitLaunchType.TWO_TO_THREE
    DisplayPresetType.THREE_TO_FOUR -> SplitLaunchType.THREE_TO_FOUR
    DisplayPresetType.THREE_TO_TWO -> SplitLaunchType.THREE_TO_TWO
    DisplayPresetType.FOUR_TO_THREE -> SplitLaunchType.FOUR_TO_THREE
}
