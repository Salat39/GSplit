package com.salat.splitpresets.domain.usecases

import com.salat.splitpresets.domain.repository.SplitPresetsRepository

class GetPresetsFlowUseCase(repository: SplitPresetsRepository) {
    val flow = repository.presetsFlow
}
