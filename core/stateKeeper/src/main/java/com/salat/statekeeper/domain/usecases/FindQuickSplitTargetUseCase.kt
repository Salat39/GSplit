package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class FindQuickSplitTargetUseCase(private val repository: StateKeeperRepository) {
    suspend fun execute() = repository.requestQuickSplitTarget()
}
