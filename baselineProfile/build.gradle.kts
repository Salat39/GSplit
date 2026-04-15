plugins {
    id(libs.plugins.androidTest.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.salat.baselineprofile"
    compileSdk = ProjectConfig.COMPILE_SDK

    compileOptions {
        sourceCompatibility = ProjectConfig.COMPATIBILITY_VERSION
        targetCompatibility = ProjectConfig.COMPATIBILITY_VERSION
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        minSdk = 28
        targetSdk = ProjectConfig.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"

    buildTypes {
        maybeCreate("car")
        maybeCreate("internal")
        maybeCreate("release")
    }

    testOptions {
        managedDevices {
            localDevices {
                // images from c:\Users\%USER_NAME%\AppData\Local\Android\Sdk\system-images\

                create("pixel3axlapi28") {
                    // Use device profiles you typically see in Android Studio.
                    device = "Pixel 3a XL"
                    // Use only API levels 27 and higher.
                    apiLevel = 28
                    // To include Google services, use "google".
                    systemImageSource = "aosp"
                }

                create("pixel4api29") {
                    device = "Pixel 4"
                    apiLevel = 29
                    systemImageSource = "google"
                    require64Bit = false
                }

                create("pixel2api30") {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "google"
                    require64Bit = true
                }

                create("pixel4aapi31") {
                    device = "Pixel 4a"
                    apiLevel = 31
                    systemImageSource = "google"
                    require64Bit = true
                }

                create("pixel5api31") {
                    device = "Pixel 5"
                    apiLevel = 31
                    systemImageSource = "google"
                    require64Bit = true
                }

                create("pixel2api32") {
                    device = "Pixel 2"
                    apiLevel = 32
                    systemImageSource = "google"
                    require64Bit = true
                }

                create("pixel6api33") {
                    device = "Pixel 6"
                    apiLevel = 33
                    systemImageSource = "aosp"
                }

                create("pixel6proapi34") {
                    device = "Pixel 6 Pro"
                    apiLevel = 34
                    systemImageSource = "google"
                    require64Bit = true
                }

                create("pixel9api35") {
                    device = "Pixel 9"
                    apiLevel = 35
                    systemImageSource = "google"
                    require64Bit = true
                }

                create("pixel8proapi36") {
                    device = "Pixel 8 Pro"
                    apiLevel = 36
                    systemImageSource = "google"
                    require64Bit = true
                }
            }
        }
    }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    useConnectedDevices = false
    // managedDevices += "pixel3axlapi28"
    // managedDevices += "pixel4api29"
    // managedDevices += "pixel2api30"
    // managedDevices += "pixel4aapi31"
    // managedDevices += "pixel5api31"
    // managedDevices += "pixel2api32"
    // managedDevices += "pixel6api33"
    managedDevices += "pixel6proapi34"
    // managedDevices += "pixel9api35"
    // managedDevices += "pixel8proapi36"
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId ?: "" }
        )
    }
}
