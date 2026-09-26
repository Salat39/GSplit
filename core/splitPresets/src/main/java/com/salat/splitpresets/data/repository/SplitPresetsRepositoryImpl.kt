package com.salat.splitpresets.data.repository

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.StringPref
import com.salat.splitpresets.data.entity.AppPresetDto
import com.salat.splitpresets.data.entity.FreeWindowPresetDto
import com.salat.splitpresets.data.entity.PresetTypeDto
import com.salat.splitpresets.data.entity.SplitPresetDto
import com.salat.splitpresets.domain.entity.AppPreset
import com.salat.splitpresets.domain.entity.FreeWindowPreset
import com.salat.splitpresets.domain.entity.PresetType
import com.salat.splitpresets.domain.entity.SplitPreset
import com.salat.splitpresets.domain.repository.SplitPresetsRepository
import com.salat.systemapps.domain.repository.SystemAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SplitPresetsRepositoryImpl(
    private val dataStore: DataStoreRepository,
    private val systemApps: SystemAppsRepository
) : SplitPresetsRepository {
    private val ioScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    private val _presetsFlow = MutableStateFlow<List<SplitPreset>>(emptyList())
    override val presetsFlow = _presetsFlow.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    init {
        ioScope.launch {
            dataStore.getStringPrefFlow(StringPref.PresetsStorage).collect {
                val data = try {
                    it.deserializeToTaskPreset()
                } catch (_: Throwable) {
                    emptyList()
                }

                _presetsFlow.emit(data.toDomain())
            }
        }
    }

    override suspend fun getFreeId() = (loadItems().maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun getPresetById(id: Long) = loadItems().find { it.id == id }?.toDomain()

    override suspend fun getAutoPlayPreset() = loadItems().find { find -> find.autoStart }?.toDomain()

    override suspend fun getPresets() = loadItems().toDomain()

    override suspend fun addPreset(preset: SplitPreset) {
        val items = loadItems()
        dataStore.save(StringPref.PresetsStorage, (items + preset.toDto()).serializeToString())
    }

    override suspend fun updatePreset(preset: SplitPreset) {
        val items = loadItems().map { if (preset.id == it.id) preset.toDto() else it }
        dataStore.save(StringPref.PresetsStorage, items.serializeToString())
    }

    override suspend fun deletePreset(id: Long) {
        val items = loadItems()
        dataStore.save(StringPref.PresetsStorage, items.filter { it.id != id }.serializeToString())
    }

    override suspend fun setAutoStart(id: Long, enable: Boolean) {
        val items = loadItems()
        val patched = if (enable) {
            items.map {
                if (it.id == id) {
                    it.copy(autoStart = true)
                } else it.copy(autoStart = false)
            }
        } else {
            items.map {
                if (it.id == id) {
                    it.copy(autoStart = false)
                } else it
            }
        }
        dataStore.save(StringPref.PresetsStorage, patched.serializeToString())
    }

    override suspend fun setDarkBackground(id: Long, enable: Boolean) {
        val items = loadItems()
        val patched = items.map {
            if (it.id == id) {
                it.copy(darkBackground = enable)
            } else it
        }
        dataStore.save(StringPref.PresetsStorage, patched.serializeToString())
    }

    override suspend fun setWindowShift(id: Long, enable: Boolean) {
        val items = loadItems()
        val patched = items.map {
            if (it.id == id) {
                it.copy(bottomWindowShift = enable)
            } else it
        }
        dataStore.save(StringPref.PresetsStorage, patched.serializeToString())
    }

    override suspend fun setQuickAccess(id: Long, enable: Boolean) {
        val items = loadItems()
        val patched = items.map {
            if (it.id == id) {
                it.copy(quickAccess = enable)
            } else it
        }
        dataStore.save(StringPref.PresetsStorage, patched.serializeToString())
    }

    override suspend fun reorderPresets(ids: List<Long>) {
        val positions = ids.withIndex().associate { (index, id) -> id to index }
        val sorted = loadItems().sortedBy { positions[it.id] ?: Int.MAX_VALUE }
        dataStore.save(StringPref.PresetsStorage, sorted.serializeToString())
    }

    private suspend fun loadItems(): List<SplitPresetDto> {
        val data = dataStore.load(StringPref.PresetsStorage)
        return try {
            data.deserializeToTaskPreset()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun List<SplitPresetDto>.serializeToString(): String {
        return json.encodeToString(this)
    }

    private fun String.deserializeToTaskPreset(): List<SplitPresetDto> {
        return json.decodeFromString(this)
    }

    private fun SplitPresetDto.toDomain() = SplitPreset(
        id = id,
        firstApp = firstApp.toDomain(),
        type = type.toDomain(),
        secondApp = secondApp.toDomain(),
        autoStart = autoStart,
        darkBackground = darkBackground,
        bottomWindowShift = bottomWindowShift,
        quickAccess = quickAccess,
        windows = windows.map { it.toDomain() },
        ratio = ratio
    )

    private fun List<SplitPresetDto>.toDomain() = map { it.toDomain() }

    private fun FreeWindowPresetDto.toDomain() = FreeWindowPreset(
        app = app.toDomain(),
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        alwaysOnTop = alwaysOnTop
    )

    private fun AppPresetDto.toDomain() = AppPreset(
        title = title,
        packageName = packageName,
        icon = systemApps.getAppIcon(packageName),
        autoPlay = autoPlay
    )

    private fun PresetTypeDto.toDomain() = when (this) {
        PresetTypeDto.HALF -> PresetType.HALF
        PresetTypeDto.ONE_TO_THREE -> PresetType.ONE_TO_THREE
        PresetTypeDto.TWO_TO_THREE -> PresetType.TWO_TO_THREE
        PresetTypeDto.THREE_TO_FOUR -> PresetType.THREE_TO_FOUR
        PresetTypeDto.THREE_TO_TWO -> PresetType.THREE_TO_TWO
        PresetTypeDto.FOUR_TO_THREE -> PresetType.FOUR_TO_THREE
        PresetTypeDto.FREE -> PresetType.FREE
        PresetTypeDto.CUSTOM -> PresetType.CUSTOM
    }

    private fun SplitPreset.toDto() = SplitPresetDto(
        firstApp = firstApp.toDto(),
        type = type.toDto(),
        secondApp = secondApp.toDto(),
        autoStart = autoStart,
        darkBackground = darkBackground,
        bottomWindowShift = bottomWindowShift,
        quickAccess = quickAccess,
        id = id,
        windows = windows.map { it.toDto() },
        ratio = ratio
    )

    private fun FreeWindowPreset.toDto() = FreeWindowPresetDto(
        app = app.toDto(),
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        alwaysOnTop = alwaysOnTop
    )

    private fun AppPreset.toDto() = AppPresetDto(
        title = title,
        packageName = packageName,
        autoPlay = autoPlay
    )

    private fun PresetType.toDto() = when (this) {
        PresetType.HALF -> PresetTypeDto.HALF
        PresetType.ONE_TO_THREE -> PresetTypeDto.ONE_TO_THREE
        PresetType.TWO_TO_THREE -> PresetTypeDto.TWO_TO_THREE
        PresetType.THREE_TO_FOUR -> PresetTypeDto.THREE_TO_FOUR
        PresetType.THREE_TO_TWO -> PresetTypeDto.THREE_TO_TWO
        PresetType.FOUR_TO_THREE -> PresetTypeDto.FOUR_TO_THREE
        PresetType.FREE -> PresetTypeDto.FREE
        PresetType.CUSTOM -> PresetTypeDto.CUSTOM
    }
}
