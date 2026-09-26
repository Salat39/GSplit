package com.salat.adb.domain.usecases

import com.salat.adb.domain.repository.AdbRepository

class ApplyRequiredSystemSettingsUseCase(private val repository: AdbRepository) {
    suspend fun execute(packageName: String) = repository.applyRequiredSystemSettings(packageName)
}
