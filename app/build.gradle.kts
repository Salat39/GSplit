import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(libs.plugins.androidApplication.get().pluginId)
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId)
    id(libs.plugins.compose.compiler.get().pluginId)
    id(libs.plugins.googleServices.get().pluginId)
    id(libs.plugins.firebase.crashlytics.get().pluginId)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

apply<BasePreset>()
apply<ComposePreset>()
apply<HiltPreset>()
apply<CoilPreset>()
apply<FirebasePreset>()

android {
    namespace = ProjectConfig.APPLICATION_ID
    compileSdk = ProjectConfig.COMPILE_SDK

    defaultConfig {
        applicationId = ProjectConfig.APPLICATION_ID
        minSdk = ProjectConfig.MIN_SDK
        targetSdk = ProjectConfig.TARGET_SDK
        versionCode = getVersionCode()
        versionName = getVersionName()
        setProperty("archivesBaseName", ProjectConfig.ARCHIVES_BASE_NAME)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("int", "RETRY_DELAY", "3")
        defaultConfig {
            buildConfigField(
                "boolean",
                "BOOT_VIA_ACCESSIBILITY_SERVICE",
                ProjectConfig.BOOT_VIA_ACCESSIBILITY_SERVICE.toString()
            )
        }
    }
    bundle {
        language {
            @Suppress("UnstableApiUsage")
            enableSplit = false
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            manifestPlaceholders.apply {
                put("applicationLabel", "@string/app_label")
                put("usesCleartextTraffic", "false")
            }
        }
        maybeCreate("internal").apply {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-I"

            manifestPlaceholders.apply {
                put("applicationLabel", "${ProjectConfig.APPLICATION_NAME} Internal")
                put("usesCleartextTraffic", "true")
            }
        }
        maybeCreate("car").apply {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            manifestPlaceholders.apply {
                put("applicationLabel", ProjectConfig.APPLICATION_NAME)
                put("usesCleartextTraffic", "true")
            }
        }
        debug {
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-D"

            manifestPlaceholders.apply {
                put("applicationLabel", "${ProjectConfig.APPLICATION_NAME} Debug")
                put("usesCleartextTraffic", "true")
            }
        }
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.COMPATIBILITY_VERSION
        targetCompatibility = ProjectConfig.COMPATIBILITY_VERSION
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Enable traffic interception
    val extDistManifest = "extras/AndroidManifest.xml"
    sourceSets["internal"].manifest.srcFile(extDistManifest)
    sourceSets["car"].manifest.srcFile(extDistManifest)
    sourceSets["debug"].manifest.srcFile(extDistManifest)

    // Authenticator package resource
    applicationVariants.all {
        outputs.all {
            resValue(
                "string", "package_name", applicationId
            )
        }
    }

    // Automatic signing and assembly
    // 1) Copy and rename the file "_secure.signing.gradle" to "secure.signing.gradle"
    // 2) You can copy it to any location and specify the path to it in the "gradle.properties" file
    // 3) Specify the necessary values to sign all builds of the application
    // 4) Run the command in the terminal "./gradlew prepareRelease"
    // 5) Wait and pick up all builds from "app/build/outputs/apk/" and "app/build/outputs/bundle/"
    // https://www.timroes.de/handling-signing-configs-with-gradle
    if (project.hasProperty("secure.signing") && project.file(project.property("secure.signing") as String).exists()) {
        apply(project.property("secure.signing"))
    }
}

tasks.withType(KotlinCompile::class.java).configureEach {
    compilerOptions {
        jvmTarget.set(ProjectConfig.JVM_TARGET)
    }
}

/* Kotlin Block - makes sure that the KSP Plugin looks at
     the right paths when it comes to generated classes*/
kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

hilt {
    enableAggregatingTask = false
}

dependencies {
    // All modules that use Hilt to generate classes must be connected
    implementation(project(Modules.CORE_BASE))
    implementation(project(Modules.CORE_RESOURCES))
    implementation(project(Modules.CORE_NAVIGATION))
    implementation(project(Modules.CORE_UI))
    implementation(project(Modules.CORE_UIKIT))
    implementation(project(Modules.CORE_PREFERENCES))
    implementation(project(Modules.CORE_SPLIT_LAUNCHER))
    implementation(project(Modules.CORE_SPLIT_PRESETS))
    implementation(project(Modules.CORE_SYSTEM_APPS))
    implementation(project(Modules.CORE_STATE_KEEPER))
    implementation(project(Modules.CORE_SCHEDULER_STORAGE))
    implementation(project(Modules.CORE_REPLACEMENT_APPS_STORAGE))
    implementation(project(Modules.CORE_FIREBASE))
    implementation(project(Modules.CORE_SCREEN_SPECS))
    implementation(project(Modules.CORE_MEDIA_MONITOR))
    implementation(project(Modules.CORE_LAUNCH_HISTORY))
    implementation(project(Modules.CORE_ADB))
    implementation(project(Modules.CORE_REMOTE_CONFIG))
    implementation(project(Modules.CORE_FILE_DOWNLOADER))

    implementation(project(Modules.FEATURE_SPLIT_LIST))
    implementation(project(Modules.FEATURE_SPLIT_ADD))
    implementation(project(Modules.FEATURE_STUB))
    implementation(project(Modules.FEATURE_SETTINGS_COMMON))
    implementation(project(Modules.FEATURE_SETTINGS_GENERAL))
    implementation(project(Modules.FEATURE_SETTINGS_SCHEDULER))
    implementation(project(Modules.FEATURE_SETTINGS_AUTOSTART))
    implementation(project(Modules.FEATURE_SETTINGS_PRESETS))
    implementation(project(Modules.FEATURE_SETTINGS_UI))
    implementation(project(Modules.FEATURE_SETTINGS_ADB))
    implementation(project(Modules.FEATURE_SETTINGS_CLOSING_OVERLAY))
    implementation(project(Modules.FEATURE_SETTINGS_APP_SWITCH_OVERLAY))
    implementation(project(Modules.FEATURE_SETTINGS_DARK_SCREEN_MODE))
    implementation(project(Modules.FEATURE_SETTINGS_WINDOW_SHIFT_MODE))
    implementation(project(Modules.FEATURE_SETTINGS_APP_TASKS))
    implementation(project(Modules.FEATURE_SETTINGS_REPLACEMENT_APPS))
    implementation(project(Modules.FEATURE_SETTINGS_API))
    implementation(project(Modules.FEATURE_OVERLAY))

    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    //implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.profileinstaller)
    //implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    "baselineProfile"(project(":baselineprofile"))
    debugImplementation(libs.androidx.ui.test.manifest)

    // Detekt only in app module
    detektPlugins(libs.detekt.formatting)
}

// Detekt config
detekt {
    ignoredBuildTypes = listOf("release")
    config.setFrom(rootProject.file("detekt-rules.yml"))
    source.setFrom(
        rootProject.file("/app/src"),
        rootProject.file("/core/base/src"),
        rootProject.file("/core/preferences/src"),
        rootProject.file("/core/ui/src"),
        rootProject.file("/core/uikit/src"),
        rootProject.file("/core/navigation/src"),
        rootProject.file("/core/coil/src"),
        rootProject.file("/core/splitLauncher/src"),
        rootProject.file("/core/splitPresets/src"),
        rootProject.file("/core/systemApps/src"),
        rootProject.file("/core/stateKeeper/src"),
        rootProject.file("/core/schedulerStorage/src"),
        rootProject.file("/core/replacementAppsStorage/src"),
        rootProject.file("/core/firebase/src"),
        rootProject.file("/core/screenSpecs/src"),
        rootProject.file("/core/mediaMonitor/src"),
        rootProject.file("/core/sign/src"),
        rootProject.file("/core/launchHistory/src"),
        rootProject.file("/core/adb/src"),
        rootProject.file("/core/remoteConfig/src"),
        rootProject.file("/core/fileDownloader/src"),
        rootProject.file("/feature/split/list/src"),
        rootProject.file("/feature/split/add/src"),
        rootProject.file("/feature/split/stub/src"),
        rootProject.file("/feature/settings/common/src"),
        rootProject.file("/feature/settings/general/src"),
        rootProject.file("/feature/settings/scheduler/src"),
        rootProject.file("/feature/settings/autostart/src"),
        rootProject.file("/feature/settings/presets/src"),
        rootProject.file("/feature/settings/ui/src"),
        rootProject.file("/feature/settings/adb/src"),
        rootProject.file("/feature/settings/closingOverlay/src"),
        rootProject.file("/feature/settings/appSwitchOverlay/src"),
        rootProject.file("/feature/settings/darkScreenMode/src"),
        rootProject.file("/feature/settings/windowShiftMode/src"),
        rootProject.file("/feature/settings/appTasks/src"),
        rootProject.file("/feature/settings/replacementApps/src"),
        rootProject.file("/feature/settings/api/src"),
        rootProject.file("/feature/overlay/src"),
    )
}
// Launch detekt by every build
tasks.getByPath("preBuild")
    .dependsOn("detekt")

afterEvaluate {
    tasks.named("kspNonMinifiedReleaseKotlin").configure {
        dependsOn("kspReleaseKotlin")
    }
}

// -----------------------------------------------------
// Create all BP and assemble all
// -----------------------------------------------------

// Step 1: Create release profiles
tasks.register("prepareAll") {
    dependsOn("generateReleaseBaselineProfile")
    finalizedBy("prepareCarBaselineProfile")
}

// Step 2: Create internal profiles
//tasks.register("prepareInternalBaselineProfile") {
//    dependsOn("generateInternalBaselineProfile")
//    finalizedBy("prepareCarBaselineProfile")
//}

// Step 3: Create car profiles
tasks.register("prepareCarBaselineProfile") {
    dependsOn("generateCarBaselineProfile")
    finalizedBy("assembleAllBuilds")
}

// Step 4: Assemble all
tasks.register("assembleAllBuilds").get()
    .dependsOn("assembleRelease")
//    .dependsOn("assembleInternal")
    .dependsOn("assembleCar")
    .dependsOn("assembleDebug")

// -----------------------------------------------------
// Create release BP and assemble release
// -----------------------------------------------------

// Step 1: Create release profiles
tasks.register("prepareRelease") {
    dependsOn("generateReleaseBaselineProfile")
    finalizedBy("assembleReleaseBuild")
}

// Step 2: Assemble release
tasks.register("assembleReleaseBuild").get()
    .dependsOn("assembleRelease")

// -----------------------------------------------------
// Create internal BP and assemble internal
// -----------------------------------------------------

// Step 1: Create internal profiles
tasks.register("prepareInternal") {
    dependsOn("generateInternalBaselineProfile")
    finalizedBy("assembleInternalBuild")
}

// Step 2: Assemble internal
tasks.register("assembleInternalBuild").get()
    .dependsOn("assembleInternal")

// -----------------------------------------------------
// Create car BP and assemble car
// -----------------------------------------------------

// Step 1: Create internal profiles
tasks.register("prepareCar") {
    dependsOn("generateCarBaselineProfile")
    finalizedBy("assembleCarBuild")
}

// Step 2: Assemble car
tasks.register("assembleCarBuild").get()
    .dependsOn("assembleCar")
