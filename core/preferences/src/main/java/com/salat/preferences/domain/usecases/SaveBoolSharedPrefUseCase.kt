package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.BoolSharedPref

class SaveBoolSharedPrefUseCase(private val preferences: PreferencesRepository) {
    fun execute(pref: BoolSharedPref, value: Boolean, commitImmediately: Boolean = false) =
        preferences.setValue(pref, value, commitImmediately)
}
