package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.AnyPref

class FlowPrefsUseCase(private val preferences: DataStoreRepository) {
    fun execute(vararg prefs: AnyPref) = preferences.getAnyPrefsFlow(*prefs)
}
