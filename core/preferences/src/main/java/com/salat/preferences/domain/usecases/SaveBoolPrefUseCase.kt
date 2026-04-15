package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref

class SaveBoolPrefUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(pref: BoolPref, value: Boolean) = preferences.save(pref, value)
}
