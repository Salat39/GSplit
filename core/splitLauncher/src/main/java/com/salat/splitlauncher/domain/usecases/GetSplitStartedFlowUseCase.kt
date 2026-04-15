package com.salat.splitlauncher.domain.usecases

import com.salat.splitlauncher.domain.repository.SplitLauncherRepository

class GetSplitStartedFlowUseCase(private val repository: SplitLauncherRepository) {
    fun execute() = repository.splitStartedFlow
}
