package com.salat.settings.add.presentation.mappers

import com.salat.settings.add.presentation.AddViewModel
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.DisplayFreeWindow
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.FreeWindowPreset
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset

fun DeviceAppInfo.toDomain() = AppPreset(
    title = appName,
    packageName = packageName,
    icon = null,
    autoPlay = autoPlay
)

internal fun DisplayFreeWindow.toDomain() = FreeWindowPreset(
    app = app.toDomain(),
    left = left,
    top = top,
    right = right,
    bottom = bottom,
    alwaysOnTop = alwaysOnTop
)

internal fun AddViewModel.ViewState.toDomainPreset(id: Long) = if (freeMode) toFreePreset(id) else toSplitPreset(id)

private fun AddViewModel.ViewState.toFreePreset(id: Long): SplitPreset? {
    val windows = freeWindows.map { it.toDomain() }
    if (windows.isEmpty()) return null
    return SplitPreset(
        firstApp = windows.first().app,
        type = PresetType.FREE,
        secondApp = windows.last().app,
        autoStart = false,
        darkBackground = false,
        bottomWindowShift = false,
        quickAccess = false,
        id = id,
        windows = windows
    )
}

private fun AddViewModel.ViewState.toSplitPreset(id: Long): SplitPreset? {
    val firstApp = topApp?.toDomain() ?: return null
    val secondApp = bottomApp?.toDomain() ?: return null
    return SplitPreset(
        firstApp = firstApp,
        type = splitForm.toDomain(),
        secondApp = secondApp,
        autoStart = false,
        darkBackground = false,
        bottomWindowShift = false,
        quickAccess = false,
        id = id,
        ratio = windowRatio
    )
}

fun SizeFormat.toDomain() = when (this) {
    SizeFormat.HALF -> PresetType.HALF
    SizeFormat.ONE_TO_THREE -> PresetType.ONE_TO_THREE
    SizeFormat.TWO_TO_THREE -> PresetType.TWO_TO_THREE
    SizeFormat.THREE_TO_FOUR -> PresetType.THREE_TO_FOUR
    SizeFormat.THREE_TO_TWO -> PresetType.THREE_TO_TWO
    SizeFormat.FOUR_TO_THREE -> PresetType.FOUR_TO_THREE
    SizeFormat.CUSTOM -> PresetType.CUSTOM
}
