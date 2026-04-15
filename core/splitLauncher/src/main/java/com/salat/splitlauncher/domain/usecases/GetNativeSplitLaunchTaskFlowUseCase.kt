package com.salat.splitlauncher.domain.usecases

import com.salat.splitlauncher.domain.repository.SplitLauncherRepository

class GetNativeSplitLaunchTaskFlowUseCase(private val repository: SplitLauncherRepository) {
    fun execute() = repository.nativeSplitLaunchTaskFlow
}
