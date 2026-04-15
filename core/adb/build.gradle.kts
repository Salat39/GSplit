plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    alias(libs.plugins.kotlinSerialization)
}

apply<BaseLibConfig>()
apply<HiltPreset>()

android {
    namespace = "com.salat.adb"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_PREFERENCES))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.timber)
    implementation(libs.adb.shell)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
