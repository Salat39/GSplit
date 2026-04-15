package com.salat.settings.list.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.launchhistory.domain.entity.LastLaunchedApp
import com.salat.launchhistory.domain.entity.LastLaunchedTask
import com.salat.launchhistory.domain.entity.LastLaunchedType
import com.salat.settings.list.presentation.entity.DisplayAppPreset
import com.salat.settings.list.presentation.entity.DisplayPresetType
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.systemapps.domain.entity.InstalledAppInfo

internal fun LastLaunchedTask.toDisplay(appsInfo: List<InstalledAppInfo>, autoStart: Boolean) = DisplaySplitPreset(
    firstApp = firstApp!!.toDisplay(appsInfo.find { app -> app.packageName == firstApp?.packageName }),
    type = type.toDisplay(),
    secondApp = secondApp!!.toDisplay(appsInfo.find { app -> app.packageName == secondApp?.packageName }),
    autoStart = autoStart,
    darkBackground = darkBackground,
    bottomWindowShift = bottomWindowShift,
    quickAccess = false,
    id = 0
)

internal fun LastLaunchedApp.toDisplay(find: InstalledAppInfo?) = DisplayAppPreset(
    title = find?.appName ?: title,
    packageName = find?.packageName ?: packageName,
    icon = if (find?.icon is Drawable) find.icon as Drawable else null,
    autoPlay = autoPlay
)

internal fun LastLaunchedType.toDisplay() = when (this) {
    LastLaunchedType.HALF -> DisplayPresetType.HALF
    LastLaunchedType.ONE_TO_THREE -> DisplayPresetType.ONE_TO_THREE
    LastLaunchedType.TWO_TO_THREE -> DisplayPresetType.TWO_TO_THREE
    LastLaunchedType.THREE_TO_FOUR -> DisplayPresetType.THREE_TO_FOUR
    LastLaunchedType.THREE_TO_TWO -> DisplayPresetType.THREE_TO_TWO
    LastLaunchedType.FOUR_TO_THREE -> DisplayPresetType.FOUR_TO_THREE
}
