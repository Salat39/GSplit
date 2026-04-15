package com.salat.systemapps.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.salat.systemapps.domain.entity.InstalledAppInfo
import com.salat.systemapps.domain.repository.SystemAppsRepository
import timber.log.Timber

class SystemAppsRepositoryImpl(private val context: Context) : SystemAppsRepository {

    override suspend fun getAllApps() = context.getInstalledApps()

    override fun getAppIcon(packageName: String): Drawable? = try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.e(e)
        null
    }

    override suspend fun getApps(vararg packageNames: String): List<InstalledAppInfo> = try {
        context.getInstalledAppsInfo(*packageNames)
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.e(e)
        emptyList()
    }

    private fun Context.getInstalledApps(): List<InstalledAppInfo> {
        val pm = packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
        val appList = mutableListOf<InstalledAppInfo>()

        for (info in resolveInfos) {
            val packageName = info.activityInfo.packageName
            val appName = info.loadLabel(pm).toString()
            val icon = info.loadIcon(pm)
            val applicationInfo = try {
                pm.getApplicationInfo(packageName, 0)
            } catch (_: Exception) {
                null
            }
            appList.add(pm.buildInstalledAppInfo(packageName, appName, icon, applicationInfo))
        }

        return appList.sortedWith(
            compareBy<InstalledAppInfo> {
                when {
                    it.packageName.lowercase().contains("yandex") -> 0
                    it.appName.lowercase().contains("tv") -> 1
                    it.appName.lowercase().contains("music") -> 2
                    it.appName.lowercase().contains("maps") -> 3
                    it.appName.lowercase().contains("radio") -> 4
                    else -> 5
                }
            }.thenBy { it.appName }
        )
    }

    private fun Context.getInstalledAppsInfo(vararg packageNames: String): List<InstalledAppInfo> {
        val pm = packageManager
        return packageNames.mapNotNull { pkgName ->
            try {
                val applicationInfo = pm.getApplicationInfo(pkgName, 0)
                val appName = pm.getApplicationLabel(applicationInfo).toString()
                val icon = pm.getApplicationIcon(applicationInfo)
                pm.buildInstalledAppInfo(pkgName, appName, icon, applicationInfo)
            } catch (e: Exception) {
                Timber.e(e)
                null
            }
        }
    }

    private fun PackageManager.buildInstalledAppInfo(
        packageName: String,
        appName: String,
        icon: Drawable,
        applicationInfo: ApplicationInfo?
    ): InstalledAppInfo {
        val isMediaApp = isMediaApp(packageName, applicationInfo)
        return InstalledAppInfo(packageName, appName, icon, isMediaApp)
    }

    private fun PackageManager.isMediaApp(packageName: String, applicationInfo: ApplicationInfo?): Boolean {
        if (applicationInfo != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (applicationInfo.category == ApplicationInfo.CATEGORY_AUDIO ||
                applicationInfo.category == ApplicationInfo.CATEGORY_VIDEO
            ) {
                return true
            }
        }

        val label = applicationInfo?.let {
            try {
                getApplicationLabel(it).toString().lowercase()
            } catch (_: Exception) {
                ""
            }
        } ?: ""
        val packageLower = packageName.lowercase()
        val mediaKeywords = listOf("music", "radio", "player", "audio", "video", "tv", "media", "murglar")
        if (mediaKeywords.any { it in label || it in packageLower }) {
            return true
        }

        val musicIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory("android.intent.category.APP_MUSIC")
        }
        val isMusic = queryIntentActivities(musicIntent, 0)
            .any { it.activityInfo.packageName == packageName }

        val videoIntent = Intent(Intent.ACTION_VIEW).apply {
            type = "video/*"
        }
        val isVideo = queryIntentActivities(videoIntent, 0)
            .any { it.activityInfo.packageName == packageName }

        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val hasMediaButtonReceiver = queryBroadcastReceivers(mediaButtonIntent, 0)
            .any { it.activityInfo.packageName == packageName }

        return isMusic || isVideo || hasMediaButtonReceiver
    }
}
