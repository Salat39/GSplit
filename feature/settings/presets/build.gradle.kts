plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    id(libs.plugins.compose.compiler.get().pluginId)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

apply<ComposeLibConfig>()
apply<ComposePreset>()
apply<NavigationPreset>()
apply<BasePreset>()
apply<HiltPreset>()

android {
    namespace = "com.salat.settings.presets"
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_RESOURCES))
    implementation(project(Modules.CORE_STATE_KEEPER))
    implementation(project(Modules.CORE_UI))
    implementation(project(Modules.CORE_UIKIT))
    implementation(project(Modules.CORE_PREFERENCES))
    implementation(project(Modules.FEATURE_SETTINGS_COMMON))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
