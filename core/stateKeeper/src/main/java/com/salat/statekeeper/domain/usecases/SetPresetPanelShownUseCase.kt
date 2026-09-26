package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class SetPresetPanelShownUseCase(private val repository: StateKeeperRepository) {
    fun execute(value: Boolean) = repository.setPresetPanelShown(value)
}
