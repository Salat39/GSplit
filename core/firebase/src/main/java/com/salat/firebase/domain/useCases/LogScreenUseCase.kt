package com.salat.firebase.domain.useCases

import com.salat.firebase.domain.repository.FirebaseRepository

class LogScreenUseCase(private val repository: FirebaseRepository) {
    suspend fun execute(screenName: String) = repository.logScreen(screenName)
}
