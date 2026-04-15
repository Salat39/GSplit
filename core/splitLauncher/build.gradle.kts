plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    alias(libs.plugins.kotlinSerialization)
}

apply<BaseLibConfig>()
apply<HiltPreset>()

android {
    namespace = "com.salat.splitlauncher"
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_STATE_KEEPER))
    implementation(project(Modules.CORE_PREFERENCES))
    implementation(project(Modules.CORE_SCREEN_SPECS))
    implementation(project(Modules.CORE_MEDIA_MONITOR))
    implementation(project(Modules.CORE_FIREBASE))
    implementation(project(Modules.CORE_LAUNCH_HISTORY))
    implementation(project(Modules.CORE_ADB))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.hiddenapibypass)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
