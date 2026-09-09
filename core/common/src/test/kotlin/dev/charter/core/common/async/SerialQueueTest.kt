package dev.charter.core.common.async

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SerialQueueTest {
    @Test
    fun `operations run one at a time in submission order`() =
        runTest {
            val queue = SerialQueue()
            val log = mutableListOf<String>()
            (1..10)
                .map { i ->
                    async {
                        queue.run {
                            delay((10 - i).toLong()) // later submissions try to finish first
                            log.add("op$i")
                        }
                    }
                }.awaitAll()

            assertThat(log)
                .containsExactly(
                    "op1",
                    "op2",
                    "op3",
                    "op4",
                    "op5",
                    "op6",
                    "op7",
                    "op8",
                    "op9",
                    "op10",
                ).inOrder()
        }

    @Test
    fun `a failing operation does not block the queue`() =
        runTest {
            val queue = SerialQueue()
            val first = runCatching { queue.run { error("first fails") } }
            val second = queue.run { "second succeeds" }

            assertThat(first.isFailure).isTrue()
            assertThat(second).isEqualTo("second succeeds")
        }
}
