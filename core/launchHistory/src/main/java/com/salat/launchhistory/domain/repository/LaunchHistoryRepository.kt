package com.salat.launchhistory.domain.repository

import com.salat.launchhistory.domain.entity.LastLaunchedApp
import com.salat.launchhistory.domain.entity.LastLaunchedTask
import kotlinx.coroutines.flow.StateFlow

interface LaunchHistoryRepository {
    val historyFlow: StateFlow<LastLaunchedTask?>

    suspend fun saveLastConfig(config: LastLaunchedTask)

    suspend fun loadLastConfig(): LastLaunchedTask?

    suspend fun patchLastConfig(firstApp: LastLaunchedApp?, secondApp: LastLaunchedApp?)
}
