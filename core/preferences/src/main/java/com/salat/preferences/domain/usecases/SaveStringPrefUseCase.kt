package com.salat.preferences.domain.usecases

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.StringPref

class SaveStringPrefUseCase(private val preferences: DataStoreRepository) {
    suspend fun execute(pref: StringPref, value: String) = preferences.save(pref, value)
}
