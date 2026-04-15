package com.salat.firebase.domain.repository

import com.salat.firebase.domain.entity.FirebasePresetData

interface FirebaseRepository {
    fun init()

    suspend fun logLaunchType(type: String)

    suspend fun logScreen(screenName: String)

    suspend fun logOpenPreset(data: FirebasePresetData)
}
