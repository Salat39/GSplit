package com.salat.preferences.domain

import androidx.datastore.preferences.core.Preferences
import com.salat.preferences.domain.entity.AnyPref
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.FloatPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.entity.StringPref
import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    val dataFlow: Flow<Preferences>

    // Int
    suspend fun save(pref: IntPref, value: Int)

    suspend fun load(pref: IntPref): Int

    suspend fun exist(pref: IntPref): Boolean

    suspend fun remove(pref: IntPref)

    fun getIntPrefFlow(pref: IntPref): Flow<Int>

    // Boolean
    suspend fun save(pref: BoolPref, value: Boolean)

    suspend fun load(pref: BoolPref): Boolean

    suspend fun exist(pref: BoolPref): Boolean

    suspend fun remove(pref: BoolPref)

    fun getBooleanPrefFlow(pref: BoolPref): Flow<Boolean>

    fun getBooleanPrefsFlow(vararg prefs: BoolPref): Flow<List<Boolean>>

    // String
    suspend fun save(pref: StringPref, value: String)

    suspend fun load(pref: StringPref): String

    suspend fun exist(pref: StringPref): Boolean

    suspend fun remove(pref: StringPref)

    fun getStringPrefFlow(pref: StringPref): Flow<String>

    fun getStringPrefsFlow(vararg prefs: StringPref): Flow<List<String>>

    // Float
    suspend fun save(pref: FloatPref, value: Float)

    suspend fun load(pref: FloatPref): Float

    suspend fun exist(pref: FloatPref): Boolean

    suspend fun remove(pref: FloatPref)

    fun getFloatPrefFlow(pref: FloatPref): Flow<Float>

    fun getFloatPrefsFlow(vararg prefs: FloatPref): Flow<List<Float>>

    // Common
    suspend fun clear()

    fun getAnyPrefsFlow(vararg prefs: AnyPref): Flow<List<Any>>
}
