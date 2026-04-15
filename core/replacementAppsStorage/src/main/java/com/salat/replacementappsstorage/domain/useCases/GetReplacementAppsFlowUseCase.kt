package com.salat.replacementappsstorage.domain.useCases

import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository

class GetReplacementAppsFlowUseCase(repository: ReplacementAppStorageRepository) {
    val flow = repository.replacementAppsFlow
}
