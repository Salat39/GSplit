package com.salat.splitlauncher.domain.usecases

import com.salat.splitlauncher.domain.repository.SplitLauncherRepository

class GetNoCaptionWindowsFlowUseCase(repository: SplitLauncherRepository) {
    val flow = repository.noCaptionWindowsFlow
}
