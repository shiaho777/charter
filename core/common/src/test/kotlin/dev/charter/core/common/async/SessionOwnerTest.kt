package dev.charter.core.common.async

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SessionOwnerTest {
    @Test
    fun `beginning a new session invalidates the previous one`() {
        val owner = SessionOwner()
        val first = owner.begin()
        assertThat(first.isActive).isTrue()

        val second = owner.begin()
        assertThat(first.isStale).isTrue()
        assertThat(second.isActive).isTrue()
    }

    @Test
    fun `cancel invalidates the current session but owner stays usable`() {
        val owner = SessionOwner()
        val first = owner.begin()
        owner.cancel()

        assertThat(first.isStale).isTrue()

        val second = owner.begin()
        assertThat(second.isActive).isTrue()
    }

    @Test
    fun `close permanently rejects new work`() {
        val owner = SessionOwner()
        val first = owner.begin()
        owner.close()

        assertThat(first.isStale).isTrue()
        assertThat(owner.isClosed).isTrue()
        val threw = runCatching { owner.begin() }
        assertThat(threw.isFailure).isTrue()
    }

    @Test
    fun `stale session result is discarded`() {
        val owner = SessionOwner()
        val session1 = owner.begin()
        val session2 = owner.begin()

        // Simulate: request 1 completes late, after session 2 began.
        if (session1.isActive) {
            fail("session1 should be stale")
        }
        // The correct handling: discard because session1.isStale.
        assertThat(session1.isStale).isTrue()
        assertThat(session2.isActive).isTrue()
    }

    private fun fail(message: String): Nothing = throw AssertionError(message)
}
