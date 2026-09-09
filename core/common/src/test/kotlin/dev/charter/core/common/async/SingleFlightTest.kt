package dev.charter.core.common.async

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SingleFlightTest {
    @Test
    fun `concurrent callers join the in-flight operation`() =
        runTest {
            val flight = SingleFlight()
            var executions = 0
            val results =
                (1..5)
                    .map {
                        async {
                            flight.run {
                                executions++
                                delay(100)
                                "single"
                            }
                        }
                    }.awaitAll()

            assertThat(results).containsExactly("single", "single", "single", "single", "single")
            assertThat(executions).isEqualTo(1)
        }

    @Test
    fun `slot clears after success so the next call runs`() =
        runTest {
            val flight = SingleFlight()
            val first = flight.run { "first" }
            val second = flight.run { "second" }

            assertThat(first).isEqualTo("first")
            assertThat(second).isEqualTo("second")
        }

    @Test
    fun `joiners share the leader's exception`() =
        runTest {
            val flight = SingleFlight()
            val joiner =
                launch {
                    runCatching {
                        flight.run {
                            delay(100)
                            error("boom")
                        }
                    }
                }
            delay(10) // let the joiner register
            val leaderResult =
                runCatching {
                    flight.run {
                        delay(100)
                        error("boom")
                    }
                }

            assertThat(leaderResult.isFailure).isTrue()
            joiner.join()
        }

    @Test
    fun `sequential callers each run their own action`() =
        runTest {
            val flight = SingleFlight()
            val log = mutableListOf<String>()
            flight.run { log.add("a") }
            flight.run { log.add("b") }

            assertThat(log).containsExactly("a", "b").inOrder()
        }
}
