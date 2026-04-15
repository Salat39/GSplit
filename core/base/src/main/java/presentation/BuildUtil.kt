package presentation

import com.salat.base.BuildConfig

@Suppress("KotlinConstantConditions")
val isCarBuildType = BuildConfig.BUILD_TYPE == "car" || BuildConfig.DEBUG

val isDebug = BuildConfig.DEBUG
