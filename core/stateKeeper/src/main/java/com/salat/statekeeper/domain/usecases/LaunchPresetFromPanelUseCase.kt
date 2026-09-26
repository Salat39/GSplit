package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository

class LaunchPresetFromPanelUseCase(private val repository: StateKeeperRepository) {
    suspend fun execute(presetId: Long) = repository.sendAccessibilityServiceEvent(
        AccessibilityServiceEvent.ReplacePreset(presetId = presetId, fromPresetPanel = true)
    )
}
