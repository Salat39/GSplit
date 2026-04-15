package com.salat.remoteconfig.domain.usecases

import com.salat.remoteconfig.domain.repository.RemoteConfigRepository

class InitRemoteConfigUseCase(private val repository: RemoteConfigRepository) {
    fun execute() = repository.init()
}
