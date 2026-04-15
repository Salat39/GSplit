package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.FloatPref

class LoadFloatPrefUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(pref: FloatPref) = preferences.load(pref)
}
