package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository

class CreateSettingsSnapshotUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute() = preferences.exportAllSettings()
}
