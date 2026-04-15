plugins {
    id(libs.plugins.androidLibrary.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
}

apply<BaseLibConfig>()
apply<HiltPreset>()

android {
    namespace = "com.salat.preferences"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("Float", "UI_SCALE", "1f")
        buildConfigField("Float", "OVERLAY_SCALE", ".8f")
        buildConfigField("Float", "OVERLAY_WINDOW_SCALE", ".9f")
        buildConfigField("boolean", "SYSTEM_BAR_COMPENSATOR", "true")
        buildConfigField("boolean", "COMPAT_PLAY", "false")
        buildConfigField("boolean", "SHIFT_BEFORE_CLOSE", "false")
        buildConfigField("int", "TOOLBAR_EXTRA_SPACE", "0")
        buildConfigField("int", "BOTTOM_WINDOW_SHIFT_SIZE", "116")
    }

    buildTypes {
        maybeCreate("car").apply {
            buildConfigField("Float", "UI_SCALE", "1.5f")
            buildConfigField("Float", "OVERLAY_SCALE", "1.2f")
            buildConfigField("Float", "OVERLAY_WINDOW_SCALE", "1.3f")
            buildConfigField("boolean", "SYSTEM_BAR_COMPENSATOR", "false")
            buildConfigField("boolean", "COMPAT_PLAY", "true")
            buildConfigField("boolean", "SHIFT_BEFORE_CLOSE", "true")
            buildConfigField(
                "int",
                "TOOLBAR_EXTRA_SPACE",
                ProjectConfig.DEFAULT_CAR_TOOLBAR_EXTRA_SPACE.toString()
            )
            buildConfigField(
                "int",
                "BOTTOM_WINDOW_SHIFT_SIZE",
                ProjectConfig.DEFAULT_CAR_BOTTOM_WINDOW_SHIFT_SIZE.toString()
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
