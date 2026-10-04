package com.salat.settings.add.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.settings.add.presentation.entity.DeviceAppInfo
import com.salat.settings.add.presentation.entity.DisplayFreeWindow
import com.salat.settings.add.presentation.entity.SizeFormat
import com.salat.splitpresets.domain.entity.FreeWindowPreset
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
            autoPlay = this.firstApp.autoPlay,
            withCaption = this.firstApp.withCaption,
            mainWindow = this.firstApp.mainWindow
        )
    }
    val secondApp = installedApps.find { it.packageName == this.secondApp.packageName }?.let { item ->
        DeviceAppInfo(
            packageName = item.packageName,
            appName = item.appName,
            icon = item.icon as? Drawable,
            isMediaApp = item.isMedia,
            autoPlay = this.secondApp.autoPlay,
            withCaption = this.secondApp.withCaption,
            mainWindow = this.secondApp.mainWindow
        )
    }
    return firstApp to secondApp
}

internal fun List<FreeWindowPreset>.toDisplay(installedApps: List<InstalledAppInfo>) = mapIndexed { index, window ->
    val installed = installedApps.find { it.packageName == window.app.packageName }
    DisplayFreeWindow(
        id = index + 1,
        app = DeviceAppInfo(
            packageName = window.app.packageName,
            appName = installed?.appName ?: window.app.title,
            icon = installed?.icon as? Drawable,
            isMediaApp = installed?.isMedia ?: false,
            autoPlay = window.app.autoPlay,
            withCaption = window.app.withCaption,
            mainWindow = window.app.mainWindow
        ),
        left = window.left,
        top = window.top,
        right = window.right,
        bottom = window.bottom,
        alwaysOnTop = window.alwaysOnTop
    )
}

internal fun PresetType.toDisplay() = when (this) {
    PresetType.HALF -> SizeFormat.HALF
    PresetType.ONE_TO_THREE -> SizeFormat.ONE_TO_THREE
    PresetType.TWO_TO_THREE -> SizeFormat.TWO_TO_THREE
    PresetType.THREE_TO_FOUR -> SizeFormat.THREE_TO_FOUR
    PresetType.THREE_TO_TWO -> SizeFormat.THREE_TO_TWO
    PresetType.FOUR_TO_THREE -> SizeFormat.FOUR_TO_THREE
    PresetType.CUSTOM -> SizeFormat.CUSTOM
    PresetType.FREE -> null
}
