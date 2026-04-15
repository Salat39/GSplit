package com.salat.mediamonitor.domain.repository

import kotlinx.coroutines.flow.Flow

interface MediaMonitorRepository {
    val mediaStateFlow: Flow<Boolean>

    suspend fun isPlaying(): Boolean
}
