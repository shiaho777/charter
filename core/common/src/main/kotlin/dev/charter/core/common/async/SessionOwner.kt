package dev.charter.core.common.async

import java.util.concurrent.atomic.AtomicInteger

/**
 * Owns the currently active operation in a replaceable sequence. Starting a
 * new session invalidates the previous one — the foundation of
 * "latest search wins" without cancelling jobs.
 *
 * The classic use: search-as-you-type. Each keystroke calls [begin]; the
 * previous request's session goes stale, and its result is discarded when it
 * arrives. Unlike cancellation, the old work still completes (no
 * CancellationException surprises) — only its result is dropped.
 *
 */
class SessionOwner {
    private val version = AtomicInteger(INITIAL_VERSION)
    private val closed =
        java.util.concurrent.atomic
            .AtomicBoolean(false)

    val isClosed: Boolean get() = closed.get()

    /** Invalidates any current session and returns a fresh one. */
    fun begin(): Session {
        check(!isClosed) { "Cannot begin a session after the owner is closed." }
        return Session(this, version.incrementAndGet())
    }

    /** Invalidates the current session; the owner stays usable. */
    fun cancel() {
        if (!closed.get()) version.incrementAndGet()
    }

    /** Permanently rejects new work and invalidates any current session. */
    fun close() {
        if (closed.compareAndSet(false, true)) version.incrementAndGet()
    }

    private fun owns(session: Session): Boolean =
        !closed.get() && session.owner === this && session.myVersion == version.get()

    class Session internal constructor(
        internal val owner: SessionOwner,
        internal val myVersion: Int,
    ) {
        /** True while this session is still the current one. */
        val isActive: Boolean get() = owner.owns(this)

        /** True once a newer session (or close) has replaced this one. */
        val isStale: Boolean get() = !isActive
    }

    private companion object {
        const val INITIAL_VERSION = 0
    }
}
