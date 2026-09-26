package com.salat.adb.domain.repository

import android.graphics.Rect
import com.salat.adb.data.entity.AdbConnectionState
import com.salat.adb.data.entity.AdbRecentTaskInfo
import kotlinx.coroutines.flow.StateFlow

interface AdbRepository {
    val connectionState: StateFlow<AdbConnectionState>

    suspend fun execute(command: String): String

    suspend fun ensureConnected(): Boolean

    suspend fun isAppInFreeform(packageName: String): Boolean?

    suspend fun isAppLaunched(packageName: String): Boolean

    suspend fun getTaskId(packageName: String): Int?

    suspend fun forceStop(packageName: String): String

    suspend fun forceStop(vararg packageNames: String): String

    suspend fun allowActivateVpnAppOp(packageName: String): String

    suspend fun applyRequiredSystemSettings(packageName: String): String

    suspend fun enablePackage(packageName: String): String

    suspend fun enableAndLaunchApp(packageName: String, launchActivity: String?): String

    suspend fun disableUserPackage(packageName: String): String

    suspend fun minimize(taskId: Int)

    suspend fun resizeNewTask(packageName: String, bounds: Rect): Boolean

    suspend fun getForegroundAppPackageName(): String?

    suspend fun getRecentTasksFromActivitiesDump(): List<AdbRecentTaskInfo>

    suspend fun pressHome()

    suspend fun pressBack()
}
