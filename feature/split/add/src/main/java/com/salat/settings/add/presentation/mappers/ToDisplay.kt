package com.salat.settings.add.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.systemapps.domain.entity.InstalledAppInfo

internal fun List<InstalledAppInfo>.toDisplay() = map {
    it.toDisplay()
}

internal fun InstalledAppInfo.toDisplay() = DeviceAppInfo(
    packageName = packageName,
    appName = appName,
    icon = if (icon is Drawable) icon as Drawable else null,
    isMediaApp = isMedia,
    autoPlay = false
)

internal fun SplitPreset.toDisplay(installedApps: List<InstalledAppInfo>): Pair<DeviceAppInfo?, DeviceAppInfo?> {
    val firstApp = installedApps.find { it.packageName == this.firstApp.packageName }?.let { item ->
        DeviceAppInfo(
            packageName = item.packageName,
            appName = item.appName,
            icon = item.icon as? Drawable,
            isMediaApp = item.isMedia,
            autoPlay = this.firstApp.autoPlay
        )
    }
    val secondApp = installedApps.find { it.packageName == this.secondApp.packageName }?.let { item ->
        DeviceAppInfo(
            packageName = item.packageName,
            appName = item.appName,
            icon = item.icon as? Drawable,
            isMediaApp = item.isMedia,
            autoPlay = this.secondApp.autoPlay
        )
    }
    return firstApp to secondApp
}

internal fun PresetType.toDisplay() = when (this) {
    PresetType.HALF -> SizeFormat.HALF
    PresetType.ONE_TO_THREE -> SizeFormat.ONE_TO_THREE
    PresetType.TWO_TO_THREE -> SizeFormat.TWO_TO_THREE
    PresetType.THREE_TO_FOUR -> SizeFormat.THREE_TO_FOUR
    PresetType.THREE_TO_TWO -> SizeFormat.THREE_TO_TWO
    PresetType.FOUR_TO_THREE -> SizeFormat.FOUR_TO_THREE
}
