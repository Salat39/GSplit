package com.salat.screenspecs.domain.repository

import android.content.Context
import com.salat.screenspecs.data.entity.SpecRotation

interface ScreenSpecsRepository {
    fun getStatusBarHeight(legacyMode: Boolean = false): Int

    fun getNavBarHeight(legacyMode: Boolean = false): Int

    fun getFreeScreenHeight(legacyMode: Boolean = false): Int

    fun getFreeScreenWidth(legacyMode: Boolean = false): Int

    fun getScreenHorizontalInsets(legacyMode: Boolean = false): Pair<Int, Int>

    fun Context.getScreenRotation(): SpecRotation
}
