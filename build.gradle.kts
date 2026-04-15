plugins {
    id(libs.plugins.androidApplication.get().pluginId) apply false
    id(libs.plugins.jetbrainsKotlinAndroid.get().pluginId) apply false
    id(libs.plugins.androidLibrary.get().pluginId) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ksp) apply false
}

val ktlint: Configuration by configurations.creating

dependencies {
    @Suppress("UnstableApiUsage")
    ktlint(libs.ktlint) {
        // this is required due to https://github.com/pinterest/ktlint/issues/1114
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

tasks.register<JavaExec>("ktlint") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Check Kotlin code style and format"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args(
        "--baseline=ktlint-baseline.xml",
        "--editorconfig=.editorconfig",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

val ktlintFormat by tasks.register<JavaExec>("ktlintFormat") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    description = "Fix Kotlin code style deviations"
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    // see https://pinterest.github.io/ktlint/install/cli/#command-line-usage for more information
    args(
        "-F",
        "--baseline=ktlint-baseline.xml",
        "--editorconfig=.editorconfig",
        "**/src/**/*.kt",
        "**.kts",
        "!**/build/**",
    )
}

// Auto code style formatting (by every build)
ktlintFormat.exec()
