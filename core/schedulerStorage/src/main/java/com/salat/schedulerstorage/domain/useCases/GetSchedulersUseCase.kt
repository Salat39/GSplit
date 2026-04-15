package com.salat.schedulerstorage.domain.useCases

import com.salat.schedulerstorage.domain.entity.ScheduledItem
import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository

class GetSchedulersUseCase(private val repository: SchedulerStorageRepository) {
    suspend fun execute(): List<ScheduledItem> = repository.getSchedulers()
}
