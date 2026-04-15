package com.salat.splitlauncher.domain.usecases

import com.salat.splitlauncher.domain.repository.SplitLauncherRepository

class GetFreedomHackFlowUseCase(private val repository: SplitLauncherRepository) {
    fun execute() = repository.freeformHackFlow
}
