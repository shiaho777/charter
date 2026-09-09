package dev.charter.core.common.async

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Shares one in-flight operation; concurrent callers join it instead of
 * starting over. The answer to "the user pulled-to-refresh twice" and
 * "the fragment re-subscribed mid-load".
 *
 * Joiners share the leader's fate — value or exception, including
 * cancellation. After completion (either way) the slot clears and the next
 * call starts fresh.
 *
 */
@Suppress("TooGenericExceptionCaught")
class SingleFlight {
    private val mutex = Mutex()
    private var current: CompletableDeferred<Any?>? = null

    suspend fun <T> run(action: suspend () -> T): T {
        val mine = CompletableDeferred<Any?>()
        val toJoin: CompletableDeferred<Any?>? =
            mutex.withLock {
                val existing = current
                if (existing != null && !existing.isCompleted) {
                    existing
                } else {
                    current = mine
                    null
                }
            }
        if (toJoin != null) {
            @Suppress("UNCHECKED_CAST")
            return toJoin.await() as T
        }
        try {
            val value = action()
            mine.complete(value)
            return value
        } catch (t: Throwable) {
            mine.completeExceptionally(t)
            throw t
        } finally {
            mutex.withLock { if (current === mine) current = null }
        }
    }
}
