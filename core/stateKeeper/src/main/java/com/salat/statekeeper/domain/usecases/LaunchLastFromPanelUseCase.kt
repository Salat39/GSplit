package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository

class LaunchLastFromPanelUseCase(private val repository: StateKeeperRepository) {
    suspend fun execute() = repository.sendAccessibilityServiceEvent(
        AccessibilityServiceEvent.LaunchLast(fromPresetPanel = true)
    )
}
