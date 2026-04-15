package com.salat.replacementappsstorage.data.repository

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.StringPref
import com.salat.replacementappsstorage.data.entity.ReplacementAppItemDto
import com.salat.replacementappsstorage.domain.entity.ReplacementAppItem
import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository
import com.salat.systemapps.domain.repository.SystemAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReplacementAppStorageRepositoryImpl(
    private val dataStore: DataStoreRepository,
    private val systemApps: SystemAppsRepository
) : ReplacementAppStorageRepository {
    private val ioScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    private val _replacementAppsFlow = MutableStateFlow<List<ReplacementAppItem>>(emptyList())
    override val replacementAppsFlow = _replacementAppsFlow.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    init {
        ioScope.launch {
            dataStore.getStringPrefFlow(StringPref.ReplacementAppsStorage).collect {
                val data = try {
                    it.deserializeToTaskPreset()
                } catch (_: Throwable) {
                    emptyList()
                }

                _replacementAppsFlow.emit(data.toDomain())
            }
        }
    }

    override suspend fun getFreeId() = (loadItems().maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun getReplacementApps() = loadItems().toDomain()

    override suspend fun addReplacementApp(item: ReplacementAppItem) {
        val items = loadItems()
        dataStore.save(StringPref.ReplacementAppsStorage, (items + item.toDto()).serializeToString())
    }

    override suspend fun deleteReplacementApp(id: Long) {
        val items = loadItems()
        dataStore.save(StringPref.ReplacementAppsStorage, items.filter { it.id != id }.serializeToString())
    }

    private suspend fun loadItems(): List<ReplacementAppItemDto> {
        val data = dataStore.load(StringPref.ReplacementAppsStorage)
        return try {
            data.deserializeToTaskPreset()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun List<ReplacementAppItemDto>.serializeToString(): String {
        return json.encodeToString(this)
    }

    private fun String.deserializeToTaskPreset(): List<ReplacementAppItemDto> {
        return json.decodeFromString(this)
    }

    private fun List<ReplacementAppItemDto>.toDomain() = map {
        ReplacementAppItem(
            id = it.id,
            title = it.title,
            packageName = it.packageName,
            icon = systemApps.getAppIcon(it.packageName),
            firstWindow = it.firstWindow,
            secondWindow = it.secondWindow,
            autoPlay = it.autoPlay
        )
    }

    private fun ReplacementAppItem.toDto() = ReplacementAppItemDto(
        id = id,
        title = title,
        packageName = packageName,
        firstWindow = firstWindow,
        secondWindow = secondWindow,
        autoPlay = autoPlay
    )
}
