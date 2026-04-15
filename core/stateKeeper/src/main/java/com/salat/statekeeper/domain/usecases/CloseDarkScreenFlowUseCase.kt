package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class CloseDarkScreenFlowUseCase(repository: StateKeeperRepository) {
    val flow = repository.closeDarkScreenEvent
}
