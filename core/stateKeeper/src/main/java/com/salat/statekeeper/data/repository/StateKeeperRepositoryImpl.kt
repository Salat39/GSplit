package com.salat.statekeeper.data.repository

import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.LaunchedWindowsConfig
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class StateKeeperRepositoryImpl : StateKeeperRepository {
    private var skipAutoLaunch = false

    // in the process of closing windows
    private var inProcessClosingWindows = false
    private var closedSessionId = 0L

    private val _closeDarkScreenEvent = MutableSharedFlow<Boolean>()
    override val closeDarkScreenEvent = _closeDarkScreenEvent.asSharedFlow()

    override suspend fun sendCloseDarkScreenEvent() = _closeDarkScreenEvent.emit(true)

    private val _accessibilityServiceEnabled = MutableStateFlow(false)
    override val accessibilityServiceEnabled = _accessibilityServiceEnabled.asStateFlow()

    private val _launchedWindows = MutableStateFlow<LaunchedWindowsConfig?>(null)
    override val launchedWindows = _launchedWindows.asStateFlow()

    override suspend fun setAccessibilityServiceEnabled(value: Boolean) = _accessibilityServiceEnabled.emit(value)

    private val accessibilityServiceEventsFlow =
        MutableSharedFlow<AccessibilityServiceEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        ) // TODO TEST WITH PARAMS

    override val accessibilityServiceEvents: SharedFlow<AccessibilityServiceEvent> =
        accessibilityServiceEventsFlow.asSharedFlow()

    override suspend fun sendAccessibilityServiceEvent(event: AccessibilityServiceEvent) =
        accessibilityServiceEventsFlow.emit(event)

    private val splitLauncherEventsFlow =
        MutableSharedFlow<SplitLauncherEvent>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        ) // TODO TEST WITH PARAMS

    override val splitLauncherEvents: SharedFlow<SplitLauncherEvent> = splitLauncherEventsFlow.asSharedFlow()

    override suspend fun sendSplitLauncherEvent(event: SplitLauncherEvent) = splitLauncherEventsFlow.emit(event)

    override fun setInProcessClosingWindows(value: Boolean, sessionId: Long) {
        if (value) closedSessionId = sessionId
        inProcessClosingWindows = value
    }

    override fun inProcessClosingWindows() = inProcessClosingWindows

    override fun getClosedSessionId() = closedSessionId

    override fun setSkipAutoLaunch(value: Boolean) {
        skipAutoLaunch = value
    }

    override fun getSkipAutoLaunch() = skipAutoLaunch

    override suspend fun setLaunchedWindows(config: LaunchedWindowsConfig?) {
        _launchedWindows.emit(config)
    }

    override fun getLaunchedWindows() = _launchedWindows.value
}
