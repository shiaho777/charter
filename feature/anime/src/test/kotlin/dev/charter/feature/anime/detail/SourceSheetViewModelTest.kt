package dev.charter.feature.anime.detail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.model.SourceStatus
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakePlaySourceAggregator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SourceSheetViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val aggregator = FakePlaySourceAggregator()

    private fun viewModel() = SourceSheetViewModel(aggregator)

    @Test
    fun `start searches every source to a terminal state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel()
            viewModel.start("测试番剧", TestBangumi.sampleEpisodes)

            viewModel.results.test {
                advanceUntilIdle()
                val results = expectMostRecentItem()
                assertThat(results).hasSize(aggregator.sourceNames.size)
                assertThat(results.map { it.status }).doesNotContain(SourceStatus.SEARCHING)
                assertThat(results.map { it.status }).doesNotContain(SourceStatus.PENDING)
                assertThat(results.all { it.roads.isNotEmpty() }).isTrue()
            }
        }

    @Test
    fun `failed source retries alone without touching others`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            aggregator.failNext["源A"] = 1
            val viewModel = viewModel()
            viewModel.start("测试番剧", TestBangumi.sampleEpisodes)
            advanceUntilIdle()

            viewModel.results.test {
                var results = expectMostRecentItem()
                assertThat(results.first { it.sourceName == "源A" }.status).isEqualTo(SourceStatus.FAILED)
                assertThat(results.first { it.sourceName == "源B" }.status).isEqualTo(SourceStatus.SUCCESS)

                viewModel.retry("源A")
                advanceUntilIdle()
                results = expectMostRecentItem()
                assertThat(results.first { it.sourceName == "源A" }.status).isEqualTo(SourceStatus.SUCCESS)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `changeKeyword re-searches that source with the new keyword`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel()
            viewModel.start("旧关键词", TestBangumi.sampleEpisodes)
            advanceUntilIdle()

            viewModel.changeKeyword("源B", "新关键词")
            advanceUntilIdle()

            viewModel.results.test {
                val results = expectMostRecentItem()
                assertThat(results.first { it.sourceName == "源B" }.keyword).isEqualTo("新关键词")
                assertThat(results.first { it.sourceName == "源A" }.keyword).isEqualTo("旧关键词")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `start is idempotent`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel()
            viewModel.start("测试番剧", TestBangumi.sampleEpisodes)
            viewModel.start("测试番剧", TestBangumi.sampleEpisodes)
            advanceUntilIdle()

            viewModel.results.test {
                assertThat(expectMostRecentItem()).hasSize(aggregator.sourceNames.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
