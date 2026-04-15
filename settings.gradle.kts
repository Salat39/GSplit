pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GSplit"
include(":app")
include(":core")
include(":core:base")
include(":core:resources")
include(":core:navigation")
include(":core:uikit")
include(":core:ui")
include(":core:preferences")
include(":core:coil")
include(":core:splitLauncher")
include(":core:splitPresets")
include(":core:systemApps")
include(":core:stateKeeper")
include(":core:schedulerStorage")
include(":core:replacementAppsStorage")
include(":core:firebase")
include(":core:screenSpecs")
include(":core:mediaMonitor")
include(":core:launchHistory")
include(":core:adb")
include(":core:remoteConfig")
include(":core:fileDownloader")
include(":feature")
include(":feature:split")
include(":feature:split:list")
include(":feature:split:add")
include(":feature:split:stub")
include(":feature:settings")
include(":feature:settings:general")
include(":feature:settings:scheduler")
include(":feature:settings:autostart")
include(":feature:settings:presets")
include(":feature:settings:ui")
include(":feature:settings:adb")
include(":feature:settings:closingOverlay")
include(":feature:settings:appSwitchOverlay")
include(":feature:settings:darkScreenMode")
include(":feature:settings:windowShiftMode")
include(":feature:settings:appTasks")
include(":feature:settings:replacementApps")
include(":feature:settings:api")
include(":feature:settings:common")
include(":feature:overlay")
include(":baselineprofile")
