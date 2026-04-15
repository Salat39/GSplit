import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object ProjectConfig {
    const val APPLICATION_NAME = "GSplit"
    const val APPLICATION_ID = "com.salat.gsplit"

    const val MIN_SDK = 24
    const val TARGET_SDK = 34
    const val COMPILE_SDK = 34

    const val VERSION_MAJOR = 2
    const val VERSION_MINOR = 0
    const val VERSION_PATCH = 2
    const val VERSION_FIX = 9

    const val VERSION_POSTFIX =
        "-Beta"
    // "-Direct"

    val ARCHIVES_BASE_NAME = "${getVersionName()}[${getVersionCode()}]GSplit"

    val JVM_TARGET = JvmTarget.JVM_17

    val COMPATIBILITY_VERSION = JavaVersion.VERSION_17

    const val BOOT_VIA_ACCESSIBILITY_SERVICE = true
    const val DEFAULT_CAR_TOOLBAR_EXTRA_SPACE = 25
    const val DEFAULT_CAR_BOTTOM_WINDOW_SHIFT_SIZE = 42
}
