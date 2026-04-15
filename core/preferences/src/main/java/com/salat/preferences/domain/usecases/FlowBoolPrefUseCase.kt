package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref

class FlowBoolPrefUseCase(private val preferences: DataStoreRepository) {
    fun execute(pref: BoolPref) = preferences.getBooleanPrefFlow(pref)
}
