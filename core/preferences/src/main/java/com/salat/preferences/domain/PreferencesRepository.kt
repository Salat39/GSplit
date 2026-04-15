package com.salat.preferences.domain

import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatSharedPref
import com.salat.preferences.domain.entity.IntSharedPref
import com.salat.preferences.domain.entity.StringSharedPref

interface PreferencesRepository {
    fun getValue(pref: StringSharedPref): String

    fun setValue(pref: StringSharedPref, value: String, commitImmediately: Boolean = false)

    fun getValue(pref: BoolSharedPref): Boolean

    fun setValue(pref: BoolSharedPref, value: Boolean, commitImmediately: Boolean = false)

    fun getValue(pref: IntSharedPref): Int

    fun setValue(pref: IntSharedPref, value: Int, commitImmediately: Boolean = false)

    fun getValue(pref: FloatSharedPref): Float

    fun setValue(pref: FloatSharedPref, value: Float, commitImmediately: Boolean = false)
}
