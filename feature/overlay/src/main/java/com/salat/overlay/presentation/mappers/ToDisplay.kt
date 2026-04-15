package com.salat.overlay.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.overlay.presentation.entity.DisplayAppPreset
import com.salat.overlay.presentation.entity.DisplayPresetType
import com.salat.overlay.presentation.entity.DisplayReplacementAppItem
import com.salat.overlay.presentation.entity.DisplaySplitPreset
import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset

internal fun List<ReplacementAppItem>.toDisplay() = map { it.toDisplay() }

internal fun ReplacementAppItem.toDisplay() = DisplayReplacementAppItem(
    title = title,
    packageName = packageName,
    firstWindow = firstWindow,
    secondWindow = secondWindow,
    autoPlay = autoPlay,
    icon = if (icon is Drawable) icon as Drawable else null,
    id = id
)

internal fun List<SplitPreset>.toDisplayPreset() = map { it.toDisplayPreset() }

internal fun SplitPreset.toDisplayPreset() = DisplaySplitPreset(
    firstApp = firstApp.toDisplayPreset(),
    type = type.toDisplayPreset(),
    secondApp = secondApp.toDisplayPreset(),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    quickAccess = quickAccess,
    id = id
)

internal fun AppPreset.toDisplayPreset() = DisplayAppPreset(
    title = title,
    packageName = packageName,
    icon = if (icon is Drawable) icon as Drawable else null,
    autoPlay = autoPlay
)

internal fun PresetType.toDisplayPreset() = when (this) {
    PresetType.HALF -> DisplayPresetType.HALF
    PresetType.ONE_TO_THREE -> DisplayPresetType.ONE_TO_THREE
    PresetType.TWO_TO_THREE -> DisplayPresetType.TWO_TO_THREE
    PresetType.THREE_TO_FOUR -> DisplayPresetType.THREE_TO_FOUR
    PresetType.THREE_TO_TWO -> DisplayPresetType.THREE_TO_TWO
    PresetType.FOUR_TO_THREE -> DisplayPresetType.FOUR_TO_THREE
}

internal fun DisplaySplitPreset.toDomainPreset() = SplitPreset(
    firstApp = firstApp.toDomainPreset(),
    type = type.toDomainPreset(),
    secondApp = secondApp.toDomainPreset(),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    quickAccess = quickAccess,
    id = id
)

internal fun DisplayAppPreset.toDomainPreset() = AppPreset(
    title = title,
    packageName = packageName,
    icon = icon,
    autoPlay = autoPlay
)

internal fun DisplayPresetType.toDomainPreset() = when (this) {
    DisplayPresetType.HALF -> PresetType.HALF
    DisplayPresetType.ONE_TO_THREE -> PresetType.ONE_TO_THREE
    DisplayPresetType.TWO_TO_THREE -> PresetType.TWO_TO_THREE
    DisplayPresetType.THREE_TO_FOUR -> PresetType.THREE_TO_FOUR
    DisplayPresetType.THREE_TO_TWO -> PresetType.THREE_TO_TWO
    DisplayPresetType.FOUR_TO_THREE -> PresetType.FOUR_TO_THREE
}
