package com.salat.schedulerstorage.domain.useCases

import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository

class GetSchedulerFlowUseCase(repository: SchedulerStorageRepository) {
    val flow = repository.schedulerFlow
}
