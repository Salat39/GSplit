package com.salat.adb.domain.repository

import com.salat.adb.data.entity.AdbConnectionState
import kotlinx.coroutines.flow.Flow

interface AdbRepository {
    val connectionState: Flow<AdbConnectionState>

    suspend fun execute(command: String): String

    suspend fun isAppInFreeform(packageName: String): Boolean?

    suspend fun getTaskId(packageName: String): Int?

    suspend fun forceStop(packageName: String): String

    suspend fun forceStop(vararg packageNames: String): String

    suspend fun minimize(taskId: Int)
}
