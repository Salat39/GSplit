package com.salat.settings.list.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.settings.list.presentation.entity.DisplayAppPreset
import com.salat.settings.list.presentation.entity.DisplayPresetType
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset

internal fun List<SplitPreset>.toDisplay() = map { it.toDisplay() }

internal fun SplitPreset.toDisplay() = DisplaySplitPreset(
    firstApp = firstApp.toDisplay(),
    type = type.toDisplay(),
    secondApp = secondApp.toDisplay(),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    quickAccess = quickAccess,
    id = id
)

internal fun AppPreset.toDisplay() = DisplayAppPreset(
    title = title,
    packageName = packageName,
    icon = if (icon is Drawable) icon as Drawable else null,
    autoPlay = autoPlay
)

internal fun PresetType.toDisplay() = when (this) {
    PresetType.HALF -> DisplayPresetType.HALF
    PresetType.ONE_TO_THREE -> DisplayPresetType.ONE_TO_THREE
    PresetType.TWO_TO_THREE -> DisplayPresetType.TWO_TO_THREE
    PresetType.THREE_TO_FOUR -> DisplayPresetType.THREE_TO_FOUR
    PresetType.THREE_TO_TWO -> DisplayPresetType.THREE_TO_TWO
    PresetType.FOUR_TO_THREE -> DisplayPresetType.FOUR_TO_THREE
}
