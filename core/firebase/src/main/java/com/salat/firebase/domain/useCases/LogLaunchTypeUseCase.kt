package com.salat.firebase.domain.useCases

import com.salat.firebase.domain.repository.FirebaseRepository

class LogLaunchTypeUseCase(private val repository: FirebaseRepository) {
    suspend fun execute(type: String) = repository.logLaunchType(type)
}
