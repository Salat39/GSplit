package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class GetPresetFreeIdUseCase(private val repository: SplitPresetsRepository) {
    suspend fun execute(): Long = repository.getFreeId()
}
