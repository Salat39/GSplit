package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.FloatSharedPref

class SaveFloatSharedPrefUseCase(private val preferences: PreferencesRepository) {
    fun execute(pref: FloatSharedPref, value: Float, commitImmediately: Boolean = false) =
        preferences.setValue(pref, value, commitImmediately)
}
