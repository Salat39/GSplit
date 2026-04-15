package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.FloatSharedPref

class LoadFloatSharedPrefUseCase(private val preferences: PreferencesRepository) {
    fun execute(pref: FloatSharedPref) = preferences.getValue(pref)
}
