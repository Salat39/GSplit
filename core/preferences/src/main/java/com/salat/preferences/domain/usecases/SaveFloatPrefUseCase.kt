package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.FloatPref

class SaveFloatPrefUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(pref: FloatPref, value: Float) = preferences.save(pref, value)
}
