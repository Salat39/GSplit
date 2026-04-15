package presentation

import timber.log.Timber

fun Long.printRuntime(tag: String = "Execute time") =
    Timber.d("[RUNTIME] $tag: ${((System.nanoTime() - this) / 1_000_000.0).roundTo(1)} ms")
