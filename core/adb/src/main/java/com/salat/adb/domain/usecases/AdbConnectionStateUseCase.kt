package com.salat.adb.domain.usecases

import com.salat.adb.domain.repository.AdbRepository

class AdbConnectionStateUseCase(repository: AdbRepository) {
    val flow = repository.connectionState
}
