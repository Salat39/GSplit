package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.IntSharedPref

class SaveIntSharedPrefUseCase(private val preferences: PreferencesRepository) {
    fun execute(pref: IntSharedPref, value: Int, commitImmediately: Boolean = false) =
        preferences.setValue(pref, value, commitImmediately)
}
