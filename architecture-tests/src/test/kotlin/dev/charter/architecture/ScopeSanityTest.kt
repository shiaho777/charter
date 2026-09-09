package dev.charter.architecture

import org.junit.Test

/**
 * Guards the scope sanity: a Konsist scope that silently sees zero files would
 * let every other rule pass vacuously. This test fails loudly instead.
 */
class ScopeSanityTest {
    @Test
    fun `scope covers all charter modules`() {
        val files = charterFiles()
        assert(files.size > 30) { "Expected 30+ Kotlin files in scope, got ${files.size}" }
    }
}
