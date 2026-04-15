package com.salat.replacementappsstorage.domain.useCases

import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository

class GetReplacementAppsUseCase(private val repository: ReplacementAppStorageRepository) {
    suspend fun execute(): List<ReplacementAppItem> = repository.getReplacementApps()
}
