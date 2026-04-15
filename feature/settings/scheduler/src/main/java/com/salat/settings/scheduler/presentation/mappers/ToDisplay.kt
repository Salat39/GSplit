package com.salat.settings.scheduler.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.schedulerstorage.domain.entity.ScheduledItem
import com.salat.settings.scheduler.presentation.entity.DeviceAppInfo
import com.salat.settings.scheduler.presentation.entity.DisplayScheduledItem
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

internal fun List<ScheduledItem>.toDisplay() = map { it.toDisplay() }

internal fun ScheduledItem.toDisplay() = DisplayScheduledItem(
    id = id,
    title = title,
    packageName = packageName,
    delay = delay,
    preTask = preTask,
    autoPlay = autoPlay,
    icon = if (icon is Drawable) icon as Drawable else null
)
