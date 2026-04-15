package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class GetSkipAutoLaunchUseCase(private val repository: StateKeeperRepository) {
    fun execute() = repository.getSkipAutoLaunch()
}
