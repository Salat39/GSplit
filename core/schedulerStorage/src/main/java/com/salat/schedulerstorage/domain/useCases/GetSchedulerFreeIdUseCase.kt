package com.salat.schedulerstorage.domain.useCases

import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository

class GetSchedulerFreeIdUseCase(private val repository: SchedulerStorageRepository) {
    suspend fun execute(): Long = repository.getFreeId()
}
