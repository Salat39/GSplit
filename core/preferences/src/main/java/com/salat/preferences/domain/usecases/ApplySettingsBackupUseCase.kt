package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository

class ApplySettingsBackupUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(data: String) = preferences.importAllSettings(data)
}
