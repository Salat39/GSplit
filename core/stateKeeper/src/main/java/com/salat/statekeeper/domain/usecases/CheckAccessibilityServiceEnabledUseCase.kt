package com.salat.statekeeper.domain.usecases

import com.salat.statekeeper.domain.repository.StateKeeperRepository

class CheckAccessibilityServiceEnabledUseCase(repository: StateKeeperRepository) {
    val flow = repository.accessibilityServiceEnabled
}
