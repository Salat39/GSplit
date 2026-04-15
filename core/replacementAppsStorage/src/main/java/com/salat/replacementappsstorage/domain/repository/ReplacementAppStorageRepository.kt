package com.salat.replacementappsstorage.domain.repository

import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import kotlinx.coroutines.flow.StateFlow

interface ReplacementAppStorageRepository {
    val replacementAppsFlow: StateFlow<List<ReplacementAppItem>>

    suspend fun getFreeId(): Long

    suspend fun getReplacementApps(): List<ReplacementAppItem>

    suspend fun addReplacementApp(item: ReplacementAppItem)

    suspend fun deleteReplacementApp(id: Long)
}
