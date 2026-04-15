plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
}

apply<BaseLibConfig>()
apply<HiltPreset>()

android {
    namespace = "com.salat.remoteconfig"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("boolean", "REMOTE_CONFIG_DEBUG", "false")
        buildConfigField("long", "REMOTE_CONFIG_FETCH_INTERVAL", "10800") // in sec (min = 780)
    }
}

dependencies {
    implementation(project(Modules.CORE_BASE))

    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
