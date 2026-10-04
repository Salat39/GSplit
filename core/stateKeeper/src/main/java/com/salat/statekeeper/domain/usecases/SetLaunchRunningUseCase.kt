package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class SetLaunchRunningUseCase(private val repository: StateKeeperRepository) {
    fun execute(running: Boolean) = repository.setLaunchRunning(running)
}
