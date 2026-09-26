package com.salat.splitlauncher.domain.repository

import com.salat.splitlauncher.domain.entity.SplitLaunchSource
import com.salat.splitlauncher.domain.entity.SplitLaunchTask
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface SplitLauncherRepository {
    val freeformHackFlow: SharedFlow<Boolean>

    // True while windows can be launched without the system caption - the setting is on and ADB is connected
    val noCaptionWindowsFlow: StateFlow<Boolean>

    val darkBackgroundFlow: SharedFlow<Boolean>

    val splitStartedFlow: SharedFlow<Pair<SplitLaunchSource, SplitLaunchTask>>

    val nativeSplitLaunchTaskFlow: SharedFlow<Pair<Any, Any>>

    suspend fun launchSplit(task: SplitLaunchTask, source: SplitLaunchSource)
}
