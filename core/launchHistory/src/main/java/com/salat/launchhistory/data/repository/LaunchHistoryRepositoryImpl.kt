package com.salat.launchhistory.data.repository

import com.salat.launchhistory.data.entity.LastLaunchedAppDto
import com.salat.launchhistory.data.entity.LastLaunchedTaskDto
import com.salat.launchhistory.data.entity.LastLaunchedTypeDto
import com.salat.launchhistory.domain.entity.LastLaunchedApp
import com.salat.launchhistory.domain.entity.LastLaunchedTask
import com.salat.launchhistory.domain.entity.LastLaunchedType
import com.salat.launchhistory.domain.repository.LaunchHistoryRepository
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.StringPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

class LaunchHistoryRepositoryImpl(private val dataStore: DataStoreRepository) : LaunchHistoryRepository {
    private val ioScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    private val _historyFlow = MutableStateFlow<LastLaunchedTask?>(null)
    override val historyFlow = _historyFlow.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    init {
        ioScope.launch {
            dataStore.getStringPrefFlow(StringPref.LaunchHistoryStorage).collect {
                val data = try {
                    it.deserializeToTaskPreset()
                } catch (_: Throwable) {
                    null
                }

                _historyFlow.emit(data?.mapToDomain())
            }
        }
    }

    override suspend fun saveLastConfig(config: LastLaunchedTask) {
        try {
            val data = config.mapToDto().serializeToString()
            dataStore.save(StringPref.LaunchHistoryStorage, data)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override suspend fun loadLastConfig(): LastLaunchedTask? {
        try {
            val current = dataStore.load(StringPref.LaunchHistoryStorage)
            val dto = current.deserializeToTaskPreset()
            return dto.mapToDomain()
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    override suspend fun patchLastConfig(firstApp: LastLaunchedApp?, secondApp: LastLaunchedApp?) {
        try {
            loadLastConfig()?.let { config ->
                val patched = config.copy(
                    firstApp = firstApp ?: config.firstApp,
                    secondApp = secondApp ?: config.secondApp
                )
                saveLastConfig(patched)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun LastLaunchedTaskDto.serializeToString(): String {
        return json.encodeToString(this)
    }

    private fun String.deserializeToTaskPreset(): LastLaunchedTaskDto {
        return json.decodeFromString(this)
    }

    private fun LastLaunchedTaskDto.mapToDomain(): LastLaunchedTask {
        return LastLaunchedTask(
            firstApp = this.firstApp?.toDomain(),
            secondApp = this.secondApp?.toDomain(),
            type = this.type.toDomainType(),
            autoStart = this.autoStart,
            darkBackground = this.darkBackground,
            bottomWindowShift = this.bottomWindowShift,
            id = this.id
        )
    }

    private fun LastLaunchedAppDto.toDomain(): LastLaunchedApp {
        return LastLaunchedApp(
            title = this.title,
            packageName = this.packageName,
            autoPlay = this.autoPlay
        )
    }

    private fun LastLaunchedTypeDto.toDomainType(): LastLaunchedType {
        return LastLaunchedType.entries.first { it.id == this.id }
    }

    private fun LastLaunchedTask.mapToDto(): LastLaunchedTaskDto {
        return LastLaunchedTaskDto(
            firstApp = this.firstApp?.toDto(),
            secondApp = this.secondApp?.toDto(),
            type = this.type.toDtoType(),
            autoStart = this.autoStart,
            darkBackground = this.darkBackground,
            bottomWindowShift = this.bottomWindowShift,
            id = this.id
        )
    }

    private fun LastLaunchedApp.toDto(): LastLaunchedAppDto {
        return LastLaunchedAppDto(
            title = this.title,
            packageName = this.packageName,
            autoPlay = this.autoPlay
        )
    }

    private fun LastLaunchedType.toDtoType(): LastLaunchedTypeDto {
        return LastLaunchedTypeDto.entries.first { it.id == this.id }
    }
}
