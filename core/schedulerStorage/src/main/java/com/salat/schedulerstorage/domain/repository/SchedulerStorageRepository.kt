package com.salat.schedulerstorage.domain.repository

import com.salat.schedulerstorage.domain.entity.ScheduledItem
import kotlinx.coroutines.flow.StateFlow

interface SchedulerStorageRepository {
    val schedulerFlow: StateFlow<List<ScheduledItem>>

    suspend fun getFreeId(): Long

    suspend fun getSchedulers(): List<ScheduledItem>

    suspend fun addScheduler(item: ScheduledItem)

    suspend fun deleteScheduler(id: Long)
}
