plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    id(libs.plugins.compose.compiler.get().pluginId)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

apply<ComposeLibConfig>()
apply<ComposePreset>()
apply<BasePreset>()

android {
    namespace = "com.salat.settings.common"
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_RESOURCES))
    implementation(project(Modules.CORE_UI))
    implementation(project(Modules.CORE_UIKIT))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
