package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.BoolSharedPref

class LoadBoolSharedPrefUseCase(private val preferences: PreferencesRepository) {
    fun execute(pref: BoolSharedPref) = preferences.getValue(pref)
}
