package com.salat.launchhistory.domain.usecases

import com.salat.launchhistory.domain.repository.LaunchHistoryRepository

class GetHistoryFlowUseCase(repository: LaunchHistoryRepository) {
    val flow = repository.historyFlow
}
