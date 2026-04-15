package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.IntPref

class LoadIntPrefUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(pref: IntPref) = preferences.load(pref)
}
