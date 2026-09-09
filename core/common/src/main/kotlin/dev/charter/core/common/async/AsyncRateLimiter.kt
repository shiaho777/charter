package dev.charter.core.common.async

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Ensures asynchronous operations are spaced apart by at least [period] —
 * API politeness, snackbar throttling, analytics batching. Slots are
 * reserved in submission order; each acquire waits for its slot.
 *
 */
class AsyncRateLimiter(
    private val periodMillis: Long,
) {
    private val mutex = Mutex()
    private var nextAllowedAt = 0L

    /** Waits until the next allowed time slot and reserves the following slot. */
    suspend fun acquire() {
        val waitMillis =
            mutex.withLock {
                val now = System.currentTimeMillis()
                val scheduledAt = if (nextAllowedAt > now) nextAllowedAt else now
                nextAllowedAt = scheduledAt + periodMillis
                scheduledAt - now
            }
        if (waitMillis > 0) delay(waitMillis)
    }

    suspend fun <T> run(action: suspend () -> T): T {
        acquire()
        return action()
    }
}
