package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.IntSharedPref

class LoadIntSharedPrefUseCase(private val preferences: PreferencesRepository) {
    fun execute(pref: IntSharedPref) = preferences.getValue(pref)
}
