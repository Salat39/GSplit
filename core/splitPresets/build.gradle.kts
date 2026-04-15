plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    alias(libs.plugins.kotlinSerialization)
}

apply<BaseLibConfig>()
apply<HiltPreset>()

android {
    namespace = "com.salat.splitpresets"
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_PREFERENCES))
    implementation(project(Modules.CORE_SYSTEM_APPS))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.timber)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
