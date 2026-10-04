package com.salat.statekeeper.domain.repository

import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.LaunchedWindowsConfig
import com.salat.statekeeper.domain.entity.QuickSplitTarget
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface StateKeeperRepository {
    fun setClosedSessionId(sessionId: Long)

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

    val importSettingsEvents: SharedFlow<Unit>

    suspend fun sendImportSettings()

    val presetPanelShown: StateFlow<Boolean>

    fun setPresetPanelShown(value: Boolean)

    val replaceMenuShown: StateFlow<Boolean>

    fun setReplaceMenuShown(value: Boolean)

    val placedWindowsSessionId: StateFlow<Long>

    fun setPlacedWindowsSessionId(sessionId: Long)

    fun setLaunchRunning(running: Boolean)

    fun isLaunchRunning(): Boolean

    // The accessibility service completes each request with the target of the quick split shortcut or null
    val quickSplitTargetRequests: SharedFlow<CompletableDeferred<QuickSplitTarget?>>

    suspend fun requestQuickSplitTarget(): QuickSplitTarget?
}
