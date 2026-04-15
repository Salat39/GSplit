package com.salat.schedulerstorage.domain.useCases

import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository

class DeleteScheduledItemUseCase(private val repository: SchedulerStorageRepository) {
    suspend fun execute(id: Long) = repository.deleteScheduler(id)
}
