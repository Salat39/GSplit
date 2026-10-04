package com.salat.settings.quicksplit.presentation.mappers

import android.graphics.drawable.Drawable
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.systemapps.domain.entity.InstalledAppInfo

private const val PACKAGE_SEPARATOR = ","

fun List<InstalledAppInfo>.toQuickSplitDisplay() = map { app ->
    DisplayQuickSplitApp(
        packageName = app.packageName,
        appName = app.appName,
        icon = app.icon as? Drawable
    )
}

fun String.toQuickSplitPackages() = split(PACKAGE_SEPARATOR).filter { it.isNotBlank() }

internal fun List<String>.toQuickSplitAppsPref() = joinToString(PACKAGE_SEPARATOR)
