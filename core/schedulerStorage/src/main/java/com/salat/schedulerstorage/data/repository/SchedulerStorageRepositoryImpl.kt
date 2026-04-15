package com.salat.schedulerstorage.data.repository

import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.StringPref
import com.salat.schedulerstorage.data.entity.ScheduledItemDto
import com.salat.schedulerstorage.domain.entity.ScheduledItem
import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository
import com.salat.systemapps.domain.repository.SystemAppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SchedulerStorageRepositoryImpl(
    private val dataStore: DataStoreRepository,
    private val systemApps: SystemAppsRepository
) : SchedulerStorageRepository {
    private val ioScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    private val _schedulerFlow = MutableStateFlow<List<ScheduledItem>>(emptyList())
    override val schedulerFlow = _schedulerFlow.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    init {
        ioScope.launch {
            dataStore.getStringPrefFlow(StringPref.SchedulerStorage).collect {
                val data = try {
                    it.deserializeToTaskPreset()
                } catch (_: Throwable) {
                    emptyList()
                }

                _schedulerFlow.emit(data.toDomain())
            }
        }
    }

    override suspend fun getFreeId() = (loadItems().maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun getSchedulers() = loadItems().toDomain()

    override suspend fun addScheduler(item: ScheduledItem) {
        val items = loadItems()
        dataStore.save(StringPref.SchedulerStorage, (items + item.toDto()).serializeToString())
    }

    override suspend fun deleteScheduler(id: Long) {
        val items = loadItems()
        dataStore.save(StringPref.SchedulerStorage, items.filter { it.id != id }.serializeToString())
    }

    private suspend fun loadItems(): List<ScheduledItemDto> {
        val data = dataStore.load(StringPref.SchedulerStorage)
        return try {
            data.deserializeToTaskPreset()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun List<ScheduledItemDto>.serializeToString(): String {
        return json.encodeToString(this)
    }

    private fun String.deserializeToTaskPreset(): List<ScheduledItemDto> {
        return json.decodeFromString(this)
    }

    private fun List<ScheduledItemDto>.toDomain() = map {
        ScheduledItem(
            id = it.id,
            title = it.title,
            packageName = it.packageName,
            icon = systemApps.getAppIcon(it.packageName),
            preTask = it.preTask,
            autoPlay = it.autoPlay,
            delay = it.delay,
        )
    }

    private fun ScheduledItem.toDto() = ScheduledItemDto(
        id = id,
        title = title,
        packageName = packageName,
        preTask = preTask,
        autoPlay = autoPlay,
        delay = delay,
    )
}
