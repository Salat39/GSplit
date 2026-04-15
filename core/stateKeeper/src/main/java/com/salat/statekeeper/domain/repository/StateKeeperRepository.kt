package com.salat.statekeeper.domain.repository

import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.LaunchedWindowsConfig
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface StateKeeperRepository {
    fun setInProcessClosingWindows(value: Boolean, sessionId: Long = 0L)

    fun inProcessClosingWindows(): Boolean

    fun getClosedSessionId(): Long

    fun setSkipAutoLaunch(value: Boolean)

    fun getSkipAutoLaunch(): Boolean

    val accessibilityServiceEnabled: StateFlow<Boolean>

    suspend fun setAccessibilityServiceEnabled(value: Boolean)

    val closeDarkScreenEvent: SharedFlow<Boolean>

    suspend fun sendCloseDarkScreenEvent()

    val accessibilityServiceEvents: SharedFlow<AccessibilityServiceEvent>

    suspend fun sendAccessibilityServiceEvent(event: AccessibilityServiceEvent)

    val splitLauncherEvents: SharedFlow<SplitLauncherEvent>

    suspend fun sendSplitLauncherEvent(event: SplitLauncherEvent)

    val launchedWindows: StateFlow<LaunchedWindowsConfig?>

    suspend fun setLaunchedWindows(config: LaunchedWindowsConfig?)

    fun getLaunchedWindows(): LaunchedWindowsConfig?
}
