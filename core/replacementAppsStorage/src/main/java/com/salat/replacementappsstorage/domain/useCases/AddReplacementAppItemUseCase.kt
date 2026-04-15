package com.salat.replacementappsstorage.domain.useCases

import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository

class AddReplacementAppItemUseCase(private val repository: ReplacementAppStorageRepository) {
    suspend fun execute(preset: ReplacementAppItem) = repository.addReplacementApp(preset)
}
