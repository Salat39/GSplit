package com.salat.splitlauncher.domain.usecases

import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import com.salat.splitlauncher.domain.repository.SplitLauncherRepository

class LaunchSplitUseCase(private val repository: SplitLauncherRepository) {
    suspend fun execute(task: SplitLaunchTask, source: SplitLaunchSource) = repository.launchSplit(task, source)
}
