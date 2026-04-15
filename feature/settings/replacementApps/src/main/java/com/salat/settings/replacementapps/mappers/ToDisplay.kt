package com.salat.settings.replacementapps.mappers

import android.graphics.drawable.Drawable
import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import com.salat.settings.replacementapps.entity.DeviceAppInfo
import com.salat.settings.replacementapps.entity.DisplayReplacementAppItem
import com.salat.systemapps.domain.entity.InstalledAppInfo

internal fun List<InstalledAppInfo>.toAppsDisplay() = map {
    it.toAppDisplay()
}

internal fun InstalledAppInfo.toAppDisplay() = DeviceAppInfo(
    packageName = packageName,
    appName = appName,
    icon = if (icon is Drawable) icon as Drawable else null,
    isMediaApp = isMedia,
    autoPlay = false
)

internal fun List<ReplacementAppItem>.toDisplay() = map { it.toDisplay() }

internal fun ReplacementAppItem.toDisplay() = DisplayReplacementAppItem(
    id = id,
    title = title,
    packageName = packageName,
    firstWindow = firstWindow,
    secondWindow = secondWindow,
    autoPlay = autoPlay,
    icon = if (icon is Drawable) icon as Drawable else null
)
