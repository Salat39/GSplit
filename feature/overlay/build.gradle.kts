plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    id(libs.plugins.compose.compiler.get().pluginId)
    alias(libs.plugins.ksp)
}

apply<ComposeLibConfig>()
apply<ComposePreset>()
apply<BasePreset>()
apply<HiltPreset>()
apply<CoilPreset>()

android {
    namespace = "com.salat.overlay"
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_RESOURCES))
    implementation(project(Modules.CORE_UI))
    implementation(project(Modules.CORE_UIKIT))
    implementation(project(Modules.CORE_PREFERENCES))
    implementation(project(Modules.CORE_STATE_KEEPER))
    implementation(project(Modules.CORE_SCREEN_SPECS))
    implementation(project(Modules.CORE_REPLACEMENT_APPS_STORAGE))
    implementation(project(Modules.CORE_SPLIT_PRESETS))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
