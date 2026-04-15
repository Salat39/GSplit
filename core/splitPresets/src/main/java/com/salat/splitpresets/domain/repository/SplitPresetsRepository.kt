package com.salat.splitpresets.domain.repository

import com.salat.splitpresets.domain.entity.SplitPreset
import kotlinx.coroutines.flow.StateFlow

interface SplitPresetsRepository {
    val presetsFlow: StateFlow<List<SplitPreset>>

    suspend fun getFreeId(): Long

    suspend fun getPresetById(id: Long): SplitPreset?

    suspend fun getAutoPlayPreset(): SplitPreset?

    suspend fun getPresets(): List<SplitPreset>

    suspend fun addPreset(preset: SplitPreset)

    suspend fun updatePreset(preset: SplitPreset)

    suspend fun deletePreset(id: Long)

    suspend fun setAutoStart(id: Long, enable: Boolean)

    suspend fun setDarkBackground(id: Long, enable: Boolean)

    suspend fun setWindowShift(id: Long, enable: Boolean)

    suspend fun setQuickAccess(id: Long, enable: Boolean)
}
