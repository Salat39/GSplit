package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref

class FlowBoolPrefsUseCase(private val preferences: DataStoreRepository) {
    fun execute(vararg prefs: BoolPref) = preferences.getBooleanPrefsFlow(*prefs)
}
