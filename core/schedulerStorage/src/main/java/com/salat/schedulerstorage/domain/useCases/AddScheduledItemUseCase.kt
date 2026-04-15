package com.salat.schedulerstorage.domain.useCases

import com.salat.schedulerstorage.domain.entity.ScheduledItem
import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository

class AddScheduledItemUseCase(private val repository: SchedulerStorageRepository) {
    suspend fun execute(preset: ScheduledItem) = repository.addScheduler(preset)
}
