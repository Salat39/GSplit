package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class RequestImportSettingsUseCase(private val repository: StateKeeperRepository) {
    suspend fun execute() = repository.sendImportSettings()
}
