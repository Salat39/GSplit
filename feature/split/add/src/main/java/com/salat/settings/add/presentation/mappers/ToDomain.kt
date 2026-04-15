package com.salat.settings.add.presentation.mappers

import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.PresetType

fun DeviceAppInfo.toDomain() = AppPreset(
    title = appName,
    packageName = packageName,
    icon = null,
    autoPlay = autoPlay
)

fun SizeFormat.toDomain() = when (this) {
    SizeFormat.HALF -> PresetType.HALF
    SizeFormat.ONE_TO_THREE -> PresetType.ONE_TO_THREE
    SizeFormat.TWO_TO_THREE -> PresetType.TWO_TO_THREE
    SizeFormat.THREE_TO_FOUR -> PresetType.THREE_TO_FOUR
    SizeFormat.THREE_TO_TWO -> PresetType.THREE_TO_TWO
    SizeFormat.FOUR_TO_THREE -> PresetType.FOUR_TO_THREE
}
