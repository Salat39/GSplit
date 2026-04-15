object Modules {
    // Core modules
    const val CORE_BASE = ":core:base" // Multi-project basic tools
    const val CORE_NAVIGATION = ":core:navigation" // Navigation graphs and tools
    const val CORE_RESOURCES = ":core:resources" // Translations and images
    const val CORE_UI = ":core:ui" // Multi-project module for working with UI
    const val CORE_UIKIT = ":core:uikit" // App-specific UI
    const val CORE_PREFERENCES = ":core:preferences" // Data storage in key -> value format
    const val CORE_COIL = ":core:coil" // An image loading library
    const val CORE_SPLIT_LAUNCHER = ":core:splitLauncher" // Launch app in split
    const val CORE_SPLIT_PRESETS = ":core:splitPresets" // Presets management in split
    const val CORE_SYSTEM_APPS = ":core:systemApps" // Manage device installed apps
    const val CORE_STATE_KEEPER = ":core:stateKeeper" // Manage general global states
    const val CORE_SCHEDULER_STORAGE = ":core:schedulerStorage" // Scheduler management
    const val CORE_REPLACEMENT_APPS_STORAGE = ":core:replacementAppsStorage" // Replacement Apps management
    const val CORE_FIREBASE = ":core:firebase" // Firebase analytics
    const val CORE_SCREEN_SPECS = ":core:screenSpecs" // Calc screen spec
    const val CORE_MEDIA_MONITOR = ":core:mediaMonitor" // Media state monitor
    const val CORE_LAUNCH_HISTORY = ":core:launchHistory" // History of launched app
    const val CORE_ADB = ":core:adb" // Adb shell
    const val CORE_REMOTE_CONFIG = ":core:remoteConfig" // Firebase remote config
    const val CORE_FILE_DOWNLOADER = ":core:fileDownloader" // Download files by url

    // Feature modules
    const val FEATURE_SPLIT_LIST = ":feature:split:list" // Presets list screen
    const val FEATURE_SPLIT_ADD = ":feature:split:add" // Create preset screen
    const val FEATURE_STUB = ":feature:split:stub" // Dark filler screen
    const val FEATURE_SETTINGS_GENERAL = ":feature:settings:general" // Settings main screen
    const val FEATURE_SETTINGS_SCHEDULER = ":feature:settings:scheduler" // Settings scheduler screen
    const val FEATURE_SETTINGS_AUTOSTART = ":feature:settings:autostart" // Settings autostart screen
    const val FEATURE_SETTINGS_PRESETS = ":feature:settings:presets" // Settings presets module
    const val FEATURE_SETTINGS_UI = ":feature:settings:ui" // Settings ui module
    const val FEATURE_SETTINGS_ADB = ":feature:settings:adb" // Settings adb module
    const val FEATURE_SETTINGS_CLOSING_OVERLAY = ":feature:settings:closingOverlay" // Settings closing overlay module
    const val FEATURE_SETTINGS_APP_SWITCH_OVERLAY = ":feature:settings:appSwitchOverlay" // app switch overlay module
    const val FEATURE_SETTINGS_DARK_SCREEN_MODE = ":feature:settings:darkScreenMode" // dark screen mode settings
    const val FEATURE_SETTINGS_WINDOW_SHIFT_MODE = ":feature:settings:windowShiftMode" // window shift mode settings
    const val FEATURE_SETTINGS_APP_TASKS = ":feature:settings:appTasks" // app tasks settings
    const val FEATURE_SETTINGS_REPLACEMENT_APPS = ":feature:settings:replacementApps" // replacement apps settings
    const val FEATURE_SETTINGS_API = ":feature:settings:api" // api settings
    const val FEATURE_SETTINGS_COMMON = ":feature:settings:common" // Common settings module
    const val FEATURE_OVERLAY = ":feature:overlay" // System overlay
}
