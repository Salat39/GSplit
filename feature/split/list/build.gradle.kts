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
apply<CoilPreset>()

android {
    namespace = "com.salat.split.list"

    buildFeatures {
        buildConfig = true
    }

    // getVersionCode()
    defaultConfig {
        defaultConfig {
            buildConfigField("int", "VERSION_CODE", getVersionCode().toString())
        }
    }
}

dependencies {
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_RESOURCES))
    implementation(project(Modules.CORE_UI))
    implementation(project(Modules.CORE_UIKIT))
    implementation(project(Modules.CORE_PREFERENCES))
    implementation(project(Modules.CORE_SPLIT_LAUNCHER))
    implementation(project(Modules.CORE_SPLIT_PRESETS))
    implementation(project(Modules.CORE_SYSTEM_APPS))
    implementation(project(Modules.CORE_LAUNCH_HISTORY))
    implementation(project(Modules.CORE_REMOTE_CONFIG))
    implementation(project(Modules.CORE_FILE_DOWNLOADER))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
