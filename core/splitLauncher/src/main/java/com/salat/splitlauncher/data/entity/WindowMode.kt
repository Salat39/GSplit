package com.salat.splitlauncher.data.entity

// Values of the hidden WindowConfiguration windowing modes
internal enum class WindowMode(val id: Int, val dumpName: String) {
    FULLSCREEN(1, "fullscreen"),
    FREEFORM(5, "freeform"),
    MULTI_WINDOW(6, "multi-window")
}
