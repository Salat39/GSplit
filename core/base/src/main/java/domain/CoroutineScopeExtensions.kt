package domain

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Launches [block] in a coroutine and automatically retries on errors.
 *
 * @param attempt  Maximum number of attempts; 0 = unlimited (infinite).
 * @param block    Suspending block to execute.
 */
fun CoroutineScope.launchWithRetry(attempt: Int = 0, block: suspend CoroutineScope.() -> Unit): Job = launch {
    var currentAttempt = 0
    while (isActive) {
        try {
            block() // If no exceptions — exit
            break
        } catch (e: CancellationException) {
            throw e // Cancellation passes through immediately
        } catch (e: Exception) {
            Timber.e(e, "Error in retry block, attempt #$currentAttempt")
            currentAttempt++

            // If a limit is set and attempts are exhausted — exit
            if (attempt in 1..currentAttempt) {
                Timber.w("Stopping retries after $currentAttempt attempts")
                break
            }

            // Increasing delay: 1s, 2s, 3s…
            delay(1_000L * (currentAttempt.coerceIn(minimumValue = 0, maximumValue = 4)))
        }
    }
}

/**
 * Repeats [block] until successful completion, cancellation, or attempts are exhausted.
 *
 * @param attempt  Maximum number of attempts; 0 = unlimited (infinite).
 * @param block    Suspending block to execute.
 */
suspend fun retryWhileActive(attempt: Int = 0, block: suspend () -> Unit) = coroutineScope {
    var currentAttempt = 0

    while (isActive) {
        try {
            block() // If no exceptions — exit the loop
            break
        } catch (e: CancellationException) {
            throw e // Allow cancellation to propagate
        } catch (e: Exception) {
            Timber.e(e, "Error in retryWhileActive, attempt #$currentAttempt")
            currentAttempt++

            // If a limit is set and attempts are exhausted — exit
            if (attempt in 1..currentAttempt) {
                Timber.w("Stopping retryWhileActive after $currentAttempt attempts")
                break
            }

            // Exponential delay: 1s, 2s, 3s…
            delay(1_000L * (currentAttempt.coerceIn(minimumValue = 0, maximumValue = 4)))
        }
    }
}
