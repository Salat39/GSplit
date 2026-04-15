package com.salat.replacementappsstorage.domain.useCases

import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository

class DeleteReplacementAppItemUseCase(private val repository: ReplacementAppStorageRepository) {
    suspend fun execute(id: Long) = repository.deleteReplacementApp(id)
}
