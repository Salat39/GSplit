package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class GetPresetsUseCase(private val repository: SplitPresetsRepository) {
    suspend fun execute(): List<SplitPreset> = repository.getPresets()
}
