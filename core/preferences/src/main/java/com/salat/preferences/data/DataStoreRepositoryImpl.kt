package com.salat.preferences.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.AnyPref
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.entity.StringPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreRepositoryImpl(private val context: Context) : DataStoreRepository {
    override val dataFlow = context.dataStore.data

    // -----------------------------------
    // Float
    // -----------------------------------

    override suspend fun save(pref: FloatPref, value: Float) {
        context.dataStore.edit { store -> store[floatPreferencesKey(pref.key)] = value }
    }

    override suspend fun load(pref: FloatPref): Float {
        return dataFlow.first()[floatPreferencesKey(pref.key)] ?: pref.default
    }

    override suspend fun exist(pref: FloatPref): Boolean {
        return dataFlow.first()[floatPreferencesKey(pref.key)] != null
    }

    override suspend fun remove(pref: FloatPref) {
        context.dataStore.edit { preferences -> preferences.remove(floatPreferencesKey(pref.key)) }
    }

    override fun getFloatPrefFlow(pref: FloatPref): Flow<Float> {
        var lastValue: Float? = null
        return dataFlow
            .map { preferences ->
                preferences[floatPreferencesKey(pref.key)] ?: pref.default
            }
            .filter { lastValue != it }
            .onEach { lastValue = it }
    }

    override fun getFloatPrefsFlow(vararg prefs: FloatPref): Flow<List<Float>> {
        val lastValues = mutableMapOf<String, Float?>()

        return dataFlow
            .map { preferences ->
                prefs.map { pref ->
                    val currentValue = preferences[floatPreferencesKey(pref.key)] ?: pref.default
                    lastValues[pref.key] = currentValue
                    currentValue
                }
            }
            .distinctUntilChanged()
    }

    // -----------------------------------
    // String
    // -----------------------------------

    override suspend fun save(pref: StringPref, value: String) {
        context.dataStore.edit { store -> store[stringPreferencesKey(pref.key)] = value }
    }

    override suspend fun load(pref: StringPref) = dataFlow.first()[stringPreferencesKey(pref.key)] ?: pref.default

    override suspend fun exist(pref: StringPref): Boolean = dataFlow.first()[stringPreferencesKey(pref.key)] != null

    override suspend fun remove(pref: StringPref) {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(pref.key))
        }
    }

    override fun getStringPrefFlow(pref: StringPref): Flow<String> {
        var lastValue: String? = null
        return dataFlow
            .map { preferences ->
                preferences[stringPreferencesKey(pref.key)] ?: pref.default
            }
            .filter { lastValue != it }
            .onEach { lastValue = it }
    }

    override fun getStringPrefsFlow(vararg prefs: StringPref): Flow<List<String>> {
        val lastValues = mutableMapOf<String, String?>()

        return dataFlow
            .map { preferences ->
                // Create a list containing the current values of all provided BoolPref
                prefs.map { pref ->
                    val currentValue = preferences[stringPreferencesKey(pref.key)] ?: pref.default
                    lastValues[pref.key] = currentValue
                    currentValue
                }
            }
            .distinctUntilChanged() // Ensure the list is only emitted when any value changes
    }

    // -----------------------------------
    // Integer
    // -----------------------------------

    override suspend fun save(pref: IntPref, value: Int) {
        context.dataStore.edit { store -> store[intPreferencesKey(pref.key)] = value }
    }

    override suspend fun load(pref: IntPref) = dataFlow.first()[intPreferencesKey(pref.key)] ?: pref.default

    override suspend fun exist(pref: IntPref) = dataFlow.first()[intPreferencesKey(pref.key)] != null

    override suspend fun remove(pref: IntPref) {
        context.dataStore.edit { preferences ->
            preferences.remove(intPreferencesKey(pref.key))
        }
    }

    /**
     * Returns a Flow that emits the current value of the specified Integer preference
     * whenever it changes in the DataStore.
     *
     * @param pref The IntPref object representing the integer preference to monitor.
     * @return A Flow that emits the Integer value of the preference whenever it changes.
     */
    override fun getIntPrefFlow(pref: IntPref): Flow<Int> {
        var lastValue: Int? = null
        return dataFlow
            .map { preferences ->
                preferences[intPreferencesKey(pref.key)] ?: pref.default
            }
            .filter { lastValue != it }
            .onEach { lastValue = it }
    }

    // -----------------------------------
    // Boolean
    // -----------------------------------

    override suspend fun save(pref: BoolPref, value: Boolean) {
        context.dataStore.edit { store -> store[booleanPreferencesKey(pref.key)] = value }
    }

    override suspend fun load(pref: BoolPref) = dataFlow.first()[booleanPreferencesKey(pref.key)] ?: pref.default

    override suspend fun exist(pref: BoolPref): Boolean = dataFlow.first()[booleanPreferencesKey(pref.key)] != null

    override suspend fun remove(pref: BoolPref) {
        context.dataStore.edit { preferences ->
            preferences.remove(booleanPreferencesKey(pref.key))
        }
    }

    /**
     * Returns a Flow that emits the current value of the specified Boolean preference
     * whenever it changes in the DataStore.
     *
     * @param pref The BoolPref object representing the boolean preference to monitor.
     * @return A Flow that emits the Boolean value of the preference whenever it changes.
     */
    override fun getBooleanPrefFlow(pref: BoolPref): Flow<Boolean> {
        var lastValue: Boolean? = null
        return dataFlow
            .map { preferences ->
                preferences[booleanPreferencesKey(pref.key)] ?: pref.default
            }
            .filter { lastValue != it }
            .onEach { lastValue = it }
    }

    /**
     * Returns a Flow that emits a list of the current values of the specified Boolean preferences
     * whenever any of them changes in the DataStore.
     *
     * @param prefs A vararg of BoolPref objects representing the boolean preferences to monitor.
     * @return A Flow that emits a list of Boolean values of the preferences whenever any of them changes.
     */
    override fun getBooleanPrefsFlow(vararg prefs: BoolPref): Flow<List<Boolean>> {
        val lastValues = mutableMapOf<String, Boolean?>()

        return dataFlow
            .map { preferences ->
                // Create a list containing the current values of all provided BoolPref
                prefs.map { pref ->
                    val currentValue = preferences[booleanPreferencesKey(pref.key)] ?: pref.default
                    lastValues[pref.key] = currentValue
                    currentValue
                }
            }
            .distinctUntilChanged() // Ensure the list is only emitted when any value changes
    }

    override suspend fun clear() {
        context.dataStore.edit { preferences ->
            // Keys that should not be cleared
            val keysToKeep = emptySet<Preferences.Key<*>>()
            preferences.asMap().keys
                .filterNot { it in keysToKeep }
                .forEach { preferences.remove(it) }
        }
    }

    override fun getAnyPrefsFlow(vararg prefs: AnyPref): Flow<List<Any>> {
        val lastValues = mutableMapOf<String, Any?>()

        return dataFlow
            .map { preferences ->
                prefs.map { pref ->
                    when (pref) {
                        is BoolPref -> {
                            val currentValue = preferences[booleanPreferencesKey(pref.key)] ?: pref.default
                            lastValues[pref.key] = currentValue
                            currentValue
                        }

                        is IntPref -> {
                            val currentValue = preferences[intPreferencesKey(pref.key)] ?: pref.default
                            lastValues[pref.key] = currentValue
                            currentValue
                        }

                        is StringPref -> {
                            val currentValue = preferences[stringPreferencesKey(pref.key)] ?: pref.default
                            lastValues[pref.key] = currentValue
                            currentValue
                        }

                        is FloatPref -> {
                            val currentValue = preferences[floatPreferencesKey(pref.key)] ?: pref.default
                            lastValues[pref.key] = currentValue
                            currentValue
                        }

                        else -> 0
                    }
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun exportAllSettings(): String = withContext(Dispatchers.IO) {
        val backup = SettingsBackup(
            dataStore = context.dataStore.data.first().toDataStorePrefEntries(),
            sharedPreferences = SHARED_PREFERENCES_BACKUP_NAMES.map { name ->
                SharedPreferencesBackup(
                    name = name,
                    entries = context.getSharedPreferences(name, Context.MODE_PRIVATE).all.toSharedPrefEntries()
                )
            }
        )

        json.encodeToString(backup)
    }

    override suspend fun importAllSettings(serialized: String) = withContext(Dispatchers.IO) {
        val backup = runCatching { json.decodeFromString<SettingsBackup>(serialized) }.getOrNull()

        if (backup != null) {
            restoreDataStore(backup.dataStore)
            restoreSharedPreferences(backup.sharedPreferences)
        } else {
            restoreDataStore(json.decodeFromString<List<PrefEntry>>(serialized))
        }
    }

    private suspend fun restoreDataStore(entries: List<PrefEntry>) {
        context.dataStore.edit { prefs ->
            prefs.clear()
            entries.forEach { entry ->
                entry.putTo(prefs)
            }
        }
    }

    private fun restoreSharedPreferences(backups: List<SharedPreferencesBackup>) {
        SHARED_PREFERENCES_BACKUP_NAMES.forEach { name ->
            val sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
            val entries = backups.firstOrNull { it.name == name }?.entries.orEmpty()
            val editor = sharedPreferences.edit().clear()

            entries.forEach { entry ->
                entry.putTo(editor)
            }

            check(editor.commit())
        }
    }

    private fun Preferences.toDataStorePrefEntries(): List<PrefEntry> = asMap().mapNotNull { (key, value) ->
        value.toPrefEntry(key.name)
    }

    private fun Map<String, *>.toSharedPrefEntries(): List<PrefEntry> = mapNotNull { (name, value) ->
        value?.toPrefEntry(name)
    }

    private fun Any.toPrefEntry(name: String): PrefEntry? = when (this) {
        is Int -> PrefEntry(name, TYPE_INT, toString())
        is Long -> PrefEntry(name, TYPE_LONG, toString())
        is Boolean -> PrefEntry(name, TYPE_BOOL, toString())
        is String -> PrefEntry(name, TYPE_STRING, this)
        is Float -> PrefEntry(name, TYPE_FLOAT, toString())
        is Double -> PrefEntry(name, TYPE_DOUBLE, toString())
        is Set<*> -> PrefEntry(name, TYPE_STRING_SET, json.encodeToString(filterIsInstance<String>().toList()))
        else -> null
    }

    private fun PrefEntry.putTo(preferences: androidx.datastore.preferences.core.MutablePreferences) {
        runCatching {
            when (type) {
                TYPE_INT -> preferences[intPreferencesKey(name)] = value.toInt()
                TYPE_LONG -> preferences[longPreferencesKey(name)] = value.toLong()
                TYPE_BOOL -> preferences[booleanPreferencesKey(name)] =
                    value.toBooleanStrictOrNull() ?: (value == "true")

                TYPE_STRING -> preferences[stringPreferencesKey(name)] = value
                TYPE_FLOAT -> preferences[floatPreferencesKey(name)] = value.toFloat()
                TYPE_DOUBLE -> preferences[doublePreferencesKey(name)] = value.toDouble()
                TYPE_STRING_SET -> preferences[stringSetPreferencesKey(name)] = decodeStringSet(value)
            }
        }
    }

    private fun PrefEntry.putTo(editor: SharedPreferences.Editor) {
        runCatching {
            when (type) {
                TYPE_INT -> editor.putInt(name, value.toInt())
                TYPE_LONG -> editor.putLong(name, value.toLong())
                TYPE_BOOL -> editor.putBoolean(name, value.toBooleanStrictOrNull() ?: (value == "true"))
                TYPE_STRING -> editor.putString(name, value)
                TYPE_FLOAT -> editor.putFloat(name, value.toFloat())
                TYPE_STRING_SET -> editor.putStringSet(name, decodeStringSet(value))
            }
        }
    }

    private fun decodeStringSet(value: String): Set<String> {
        return runCatching { json.decodeFromString<List<String>>(value).toSet() }.getOrElse { emptySet() }
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    private data class SettingsBackup(
        @SerialName("version") val version: Int = BACKUP_VERSION,
        @SerialName("dataStore") val dataStore: List<PrefEntry> = emptyList(),
        @SerialName("sharedPreferences") val sharedPreferences: List<SharedPreferencesBackup> = emptyList()
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    private data class SharedPreferencesBackup(
        @SerialName("name") val name: String,
        @SerialName("entries") val entries: List<PrefEntry> = emptyList()
    )

    @SuppressLint("UnsafeOptInUsageError")
    @Serializable
    private data class PrefEntry(
        @SerialName("name") val name: String,
        @SerialName("type") val type: String,
        @SerialName("value") val value: String
    )

    companion object {
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data")

        private const val BACKUP_VERSION = 2
        private const val SHARED_PREFERENCES_APP_CONFIG = "app_config"
        private const val SHARED_PREFERENCES_AUTO_LAUNCH = "auto_launch_prefs"
        private val SHARED_PREFERENCES_BACKUP_NAMES = listOf(
            SHARED_PREFERENCES_APP_CONFIG,
            SHARED_PREFERENCES_AUTO_LAUNCH
        )
        private const val TYPE_INT = "int"
        private const val TYPE_LONG = "long"
        private const val TYPE_BOOL = "bool"
        private const val TYPE_STRING = "string"
        private const val TYPE_FLOAT = "float"
        private const val TYPE_DOUBLE = "double"
        private const val TYPE_STRING_SET = "stringSet"

        private val json: Json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            prettyPrint = false
        }
    }
}
