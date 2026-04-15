package com.salat.preferences.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatSharedPref
import com.salat.preferences.domain.entity.IntSharedPref
import com.salat.preferences.domain.entity.StringSharedPref

class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    override fun getValue(pref: StringSharedPref) = sharedPreferences.getString(pref.key, pref.default) ?: pref.default

    @SuppressLint("ApplySharedPref")
    override fun setValue(pref: StringSharedPref, value: String, commitImmediately: Boolean) {
        sharedPreferences.edit().putString(pref.key, value).also {
            if (commitImmediately) it.commit() else it.apply()
        }
    }

    override fun getValue(pref: BoolSharedPref) = sharedPreferences.getBoolean(pref.key, pref.default)

    @SuppressLint("ApplySharedPref")
    override fun setValue(pref: BoolSharedPref, value: Boolean, commitImmediately: Boolean) {
        sharedPreferences.edit().putBoolean(pref.key, value).also {
            if (commitImmediately) it.commit() else it.apply()
        }
    }

    override fun getValue(pref: IntSharedPref) = sharedPreferences.getInt(pref.key, pref.default)

    @SuppressLint("ApplySharedPref")
    override fun setValue(pref: IntSharedPref, value: Int, commitImmediately: Boolean) {
        sharedPreferences.edit().putInt(pref.key, value).also {
            if (commitImmediately) it.commit() else it.apply()
        }
    }

    override fun getValue(pref: FloatSharedPref) = sharedPreferences.getFloat(pref.key, pref.default)

    @SuppressLint("ApplySharedPref")
    override fun setValue(pref: FloatSharedPref, value: Float, commitImmediately: Boolean) {
        sharedPreferences.edit().putFloat(pref.key, value).also {
            if (commitImmediately) it.commit() else it.apply()
        }
    }
}
