package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class ReorderSplitPresetsUseCase(private val repository: SplitPresetsRepository) {
    suspend fun execute(ids: List<Long>) = repository.reorderPresets(ids)
}
