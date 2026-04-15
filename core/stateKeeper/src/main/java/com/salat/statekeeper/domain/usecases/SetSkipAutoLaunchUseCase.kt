package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class SetSkipAutoLaunchUseCase(private val repository: StateKeeperRepository) {
    fun execute(value: Boolean) = repository.setSkipAutoLaunch(value)
}
