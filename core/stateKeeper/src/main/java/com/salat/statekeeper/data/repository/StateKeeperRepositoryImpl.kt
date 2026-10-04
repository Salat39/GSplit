package com.salat.statekeeper.data.repository

import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.entity.LaunchedWindowsConfig
import com.salat.statekeeper.domain.entity.QuickSplitTarget
import com.salat.statekeeper.domain.entity.SplitLauncherEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull

class StateKeeperRepositoryImpl : StateKeeperRepository {
    private var skipAutoLaunch = false

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

    override fun setClosedSessionId(sessionId: Long) {
        closedSessionId = sessionId
    }

    override fun getClosedSessionId() = closedSessionId

    override fun setSkipAutoLaunch(value: Boolean) {
        skipAutoLaunch = value
    }

    override fun getSkipAutoLaunch() = skipAutoLaunch

    override suspend fun setLaunchedWindows(config: LaunchedWindowsConfig?) {
        _launchedWindows.emit(config)
    }

    override fun getLaunchedWindows() = _launchedWindows.value

    private val _importSettingsEvents = MutableSharedFlow<Unit>()
    override val importSettingsEvents = _importSettingsEvents.asSharedFlow()

    override suspend fun sendImportSettings() {
        _importSettingsEvents.emit(Unit)
    }

    private val _presetPanelShown = MutableStateFlow(false)
    override val presetPanelShown = _presetPanelShown.asStateFlow()

    override fun setPresetPanelShown(value: Boolean) {
        _presetPanelShown.value = value
    }

    private val _replaceMenuShown = MutableStateFlow(false)
    override val replaceMenuShown = _replaceMenuShown.asStateFlow()

    override fun setReplaceMenuShown(value: Boolean) {
        _replaceMenuShown.value = value
    }

    private val _placedWindowsSessionId = MutableStateFlow(0L)
    override val placedWindowsSessionId = _placedWindowsSessionId.asStateFlow()

    // A late signal of an older launch must not replace the signal of a newer session
    override fun setPlacedWindowsSessionId(sessionId: Long) {
        _placedWindowsSessionId.update { maxOf(it, sessionId) }
    }

    // Launches can overlap. The flag stays on until the last launch ends
    private val runningLaunches = AtomicInteger()

    override fun setLaunchRunning(running: Boolean) {
        if (running) runningLaunches.incrementAndGet() else runningLaunches.decrementAndGet()
    }

    override fun isLaunchRunning() = runningLaunches.get() > 0

    private val _quickSplitTargetRequests = MutableSharedFlow<CompletableDeferred<QuickSplitTarget?>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val quickSplitTargetRequests = _quickSplitTargetRequests.asSharedFlow()

    override suspend fun requestQuickSplitTarget(): QuickSplitTarget? {
        if (!accessibilityServiceEnabled.value) return null
        val reply = CompletableDeferred<QuickSplitTarget?>()
        // The request is cancelled after the timeout. The service does not act on a late request
        return try {
            _quickSplitTargetRequests.emit(reply)
            withTimeoutOrNull(QUICK_SPLIT_TARGET_TIMEOUT) { reply.await() }
        } finally {
            reply.cancel()
        }
    }

    private companion object {
        const val QUICK_SPLIT_TARGET_TIMEOUT = 1_000L
    }
}
