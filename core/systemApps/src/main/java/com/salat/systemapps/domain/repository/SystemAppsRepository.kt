package com.salat.systemapps.domain.repository

import android.graphics.drawable.Drawable
import com.salat.systemapps.domain.entity.InstalledAppInfo

interface SystemAppsRepository {
    suspend fun getAllApps(): List<InstalledAppInfo>

    suspend fun getApps(vararg packageNames: String): List<InstalledAppInfo>

    fun getAppIcon(packageName: String): Drawable?
}
