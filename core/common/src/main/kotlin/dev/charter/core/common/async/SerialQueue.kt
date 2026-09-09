package dev.charter.core.common.async

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs asynchronous operations one at a time in submission order. Kotlin's
 * fair Mutex already provides FIFO serialization — this class exists to name
 * that intent at the call site ("these writes must not interleave") rather
 * than sprinkling bare mutexes through ViewModels.
 *
 * A failing operation does not block the queue: the lock releases on
 * exception and the next queued operation runs.
 *
 */
class SerialQueue {
    private val mutex = Mutex()

    suspend fun <T> run(action: suspend () -> T): T = mutex.withLock { action() }
}
