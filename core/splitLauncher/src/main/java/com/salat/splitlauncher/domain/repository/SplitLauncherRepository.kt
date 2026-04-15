package com.salat.splitlauncher.domain.repository

import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import kotlinx.coroutines.flow.SharedFlow

interface SplitLauncherRepository {
    val freeformHackFlow: SharedFlow<Boolean>

    val darkBackgroundFlow: SharedFlow<Boolean>

    val splitStartedFlow: SharedFlow<Pair<SplitLaunchSource, SplitLaunchTask>>

    val nativeSplitLaunchTaskFlow: SharedFlow<Pair<Any, Any>>

    suspend fun launchSplit(task: SplitLaunchTask, source: SplitLaunchSource)
}
