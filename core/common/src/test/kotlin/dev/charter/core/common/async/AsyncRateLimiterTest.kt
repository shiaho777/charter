package dev.charter.core.common.async

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AsyncRateLimiterTest {
    @Test
    fun `operations are spaced apart by the period`() =
        runTest {
            val limiter = AsyncRateLimiter(periodMillis = 100)
            val timestamps = mutableListOf<Long>()
            (1..3)
                .map {
                    async {
                        limiter.run {
                            timestamps.add(testScheduler.currentTime)
                        }
                    }
                }.awaitAll()

            // All three should have distinct, spaced-out slots.
            assertThat(timestamps).hasSize(3)
            assertThat(timestamps[0]).isEqualTo(0L)
            assertThat(timestamps[1]).isEqualTo(100L)
            assertThat(timestamps[2]).isEqualTo(200L)
        }

    @Test
    fun `first operation runs immediately`() =
        runTest {
            val limiter = AsyncRateLimiter(periodMillis = 50)
            val start = testScheduler.currentTime
            limiter.acquire()
            // Virtual time hasn't advanced — acquire happened instantly.
            assertThat(testScheduler.currentTime).isEqualTo(start)
        }
}
