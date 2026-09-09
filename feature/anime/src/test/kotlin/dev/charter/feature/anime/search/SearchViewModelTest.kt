package dev.charter.feature.anime.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeBangumiRepository
import dev.charter.feature.anime.FakeSearchHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bangumiRepository = FakeBangumiRepository()
    private val historyRepository = FakeSearchHistoryRepository()

    private fun viewModel() = SearchViewModel(bangumiRepository, historyRepository)

    @Test
    fun `blank query idles with history`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            historyRepository.entries.value = TestBangumi.searchHistory("eva", "frieren")
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val idle = expectMostRecentItem()
                assertThat(idle).isInstanceOf(SearchUiState.Idle::class.java)
                assertThat((idle as SearchUiState.Idle).history).hasSize(2)
            }
        }

    @Test
    fun `settled query searches and records history`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.searchItems.value = TestBangumi.sampleSeason
            bangumiRepository.searchResult = Result.Success(TestBangumi.sampleSeason.size)
            val viewModel = viewModel()

            viewModel.onQueryChange("eva")
            advanceUntilIdle()

            viewModel.uiState.test {
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(SearchUiState.Content::class.java)
                assertThat((content as SearchUiState.Content).query).isEqualTo("eva")
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(historyRepository.entries.value.map { it.keyword }).contains("eva")
        }

    @Test
    fun `search failure becomes Error state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.searchResult = Result.Failure(AppError.Network("down"))
            val viewModel = viewModel()

            viewModel.onQueryChange("eva")

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(SearchUiState.Error::class.java)
            }
        }

    @Test
    fun `zero results become Empty state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.searchItems.value = emptyList()
            val viewModel = viewModel()

            viewModel.onQueryChange("zzz")

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(SearchUiState.Empty::class.java)
            }
        }
}
