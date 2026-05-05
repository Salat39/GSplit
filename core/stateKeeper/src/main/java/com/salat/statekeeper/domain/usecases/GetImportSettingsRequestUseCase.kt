package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

@Suppress("UseDataClass")
class GetImportSettingsRequestUseCase(private val repository: StateKeeperRepository) {
    val flow
        get() = repository.importSettingsEvents
}
