package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class DeleteSplitPresetUseCase(private val repository: SplitPresetsRepository) {
    suspend fun execute(id: Long) = repository.deletePreset(id)
}
