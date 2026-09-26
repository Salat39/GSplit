package com.salat.screenspecs.domain.repository

import android.content.Context
import com.salat.screenspecs.data.entity.SpecRotation

interface ScreenSpecsRepository {
    fun getStatusBarHeight(): Int

    fun getNavBarHeight(): Int

    fun getFreeScreenHeight(): Int

    fun getFreeScreenWidth(): Int

    fun getScreenHorizontalInsets(): Pair<Int, Int>

    fun Context.getScreenRotation(): SpecRotation
}
