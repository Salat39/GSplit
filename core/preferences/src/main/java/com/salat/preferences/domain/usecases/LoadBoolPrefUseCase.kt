package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref

class LoadBoolPrefUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(pref: BoolPref) = preferences.load(pref)
}
