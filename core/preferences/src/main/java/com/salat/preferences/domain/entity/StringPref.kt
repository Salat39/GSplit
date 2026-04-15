package com.salat.preferences.domain.entity

private object StringPrefKey {
    const val PRESETS_STORAGE = "PRESETS_STORAGE"
    const val SCHEDULER_STORAGE = "SCHEDULER_STORAGE"
    const val REPLACEMENT_APPS_STORAGE = "REPLACEMENT_APPS_STORAGE"
    const val LAUNCH_HISTORY_STORAGE = "LAUNCH_HISTORY_STORAGE"
}

sealed class StringPref(override val key: String, override val default: String) : AnyPref {
    data object PresetsStorage : StringPref(StringPrefKey.PRESETS_STORAGE, "")
    data object SchedulerStorage : StringPref(StringPrefKey.SCHEDULER_STORAGE, "")
    data object ReplacementAppsStorage : StringPref(StringPrefKey.REPLACEMENT_APPS_STORAGE, "")
    data object LaunchHistoryStorage : StringPref(StringPrefKey.LAUNCH_HISTORY_STORAGE, "")
}
