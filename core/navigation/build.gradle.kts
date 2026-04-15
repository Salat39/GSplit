plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    alias(libs.plugins.kotlinSerialization)
}

apply<BaseLibConfig>()
apply<NavigationPreset>()

android {
    namespace = "com.salat.navigation"
}

dependencies {
    implementation(project(Modules.FEATURE_SPLIT_LIST))
    implementation(project(Modules.FEATURE_SPLIT_ADD))
    implementation(project(Modules.FEATURE_STUB))
    implementation(project(Modules.FEATURE_SETTINGS_GENERAL))
    implementation(project(Modules.FEATURE_SETTINGS_SCHEDULER))
    implementation(project(Modules.FEATURE_SETTINGS_AUTOSTART))
    implementation(project(Modules.FEATURE_SETTINGS_PRESETS))
    implementation(project(Modules.FEATURE_SETTINGS_UI))
    implementation(project(Modules.FEATURE_SETTINGS_ADB))
    implementation(project(Modules.FEATURE_SETTINGS_CLOSING_OVERLAY))
    implementation(project(Modules.FEATURE_SETTINGS_APP_SWITCH_OVERLAY))
    implementation(project(Modules.FEATURE_SETTINGS_DARK_SCREEN_MODE))
    implementation(project(Modules.FEATURE_SETTINGS_WINDOW_SHIFT_MODE))
    implementation(project(Modules.FEATURE_SETTINGS_APP_TASKS))
    implementation(project(Modules.FEATURE_SETTINGS_REPLACEMENT_APPS))
    implementation(project(Modules.FEATURE_SETTINGS_API))

    implementation(libs.androidx.animation.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
