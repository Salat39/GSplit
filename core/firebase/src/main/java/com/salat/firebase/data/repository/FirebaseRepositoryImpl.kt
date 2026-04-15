package com.salat.firebase.data.repository

import android.content.Context
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.salat.firebase.BuildConfig
import com.salat.firebase.domain.entity.FirebaseEvent
import com.salat.firebase.domain.entity.FirebasePresetData
import com.salat.firebase.domain.repository.FirebaseRepository
import java.util.Locale
import timber.log.Timber

class FirebaseRepositoryImpl(
    private val context: Context
) : FirebaseRepository {

    private val lang by lazy { Locale.getDefault().country.lowercase() }

    companion object {
        private const val LOG_TAG = "[A][FIREBASE]"
    }

    override fun init() {
        FirebaseApp.initializeApp(context)
        Timber.d("$LOG_TAG Ready")
    }

    override suspend fun logLaunchType(type: String) {
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseEvent.LAUNCH_TYPE) {
            param("type", type)
            param("l", lang)
            param("v", BuildConfig.VERSION_NAME)
        }
        Timber.d("$LOG_TAG log launch type: $type")
    }

    override suspend fun logScreen(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            putString("l", lang)
            putString("v", BuildConfig.VERSION_NAME)
        }
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        Timber.d("$LOG_TAG log screen: $bundle")
    }

    override suspend fun logOpenPreset(data: FirebasePresetData) {
        val params = Bundle().apply {
            putString("app1", data.firstPackage)
            putString("app2", data.secondPackage)
            putString("type", data.type)
            putString("src", data.source)
            putString("atSt", data.autoStart.toValue())
            putString("dkBg", data.darkBackground.toValue())
            putString("st", data.bottomWindowShift.toValue())
            putString("sKill", data.softKillApp.toValue())
            putString("mBySt", data.minimizeByStart.toValue())
            putString("mByASt", data.minimizeByAutostart.toValue())
            putString("ymC", data.ymCompatPlay.toValue())
            putString("l", lang)
            putString("v", BuildConfig.VERSION_NAME)
        }
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseEvent.START_PRESET, params)

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseEvent.APP) {
            param("app", data.firstPackage)
        }
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseEvent.APP) {
            param("app", data.secondPackage)
        }

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseEvent.TYPE) {
            param("type", data.type)
        }

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseEvent.SOURCE) {
            param("source", data.source)
        }

        Timber.d("$LOG_TAG log preset: $params")
    }

    private fun Boolean.toValue() = if (this) "1" else "0"
}
