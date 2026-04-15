package com.salat.replacementappsstorage.domain.useCases

import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository

class GetReplacementAppsFreeIdUseCase(private val repository: ReplacementAppStorageRepository) {
    suspend fun execute(): Long = repository.getFreeId()
}
