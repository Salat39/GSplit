package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class SetQuickAccessSplitPresetUseCase(private val repository: SplitPresetsRepository) {
    suspend fun execute(id: Long, enable: Boolean) = repository.setQuickAccess(id, enable)
}
