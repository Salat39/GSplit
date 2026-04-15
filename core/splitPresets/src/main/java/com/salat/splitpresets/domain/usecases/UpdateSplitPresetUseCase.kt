package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class UpdateSplitPresetUseCase(private val repository: SplitPresetsRepository) {
    suspend fun execute(preset: SplitPreset) = repository.updatePreset(preset)
}
