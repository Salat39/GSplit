package com.salat.splitlauncher.domain.usecases

import com.salat.splitlauncher.domain.repository.SplitLauncherRepository

class GetMainWindowAvailableFlowUseCase(repository: SplitLauncherRepository) {
    val flow = repository.mainWindowAvailableFlow
}
