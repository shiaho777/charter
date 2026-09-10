package dev.charter.feature.anime.popular

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeBangumiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PopularViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBangumiRepository()

    @Test
    fun `cached season items become Content state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.seasonItems.value = TestBangumi.sampleSeason
            val viewModel = PopularViewModel(repository)

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(PopularUiState.Content::class.java)
                assertThat((content as PopularUiState.Content).items).hasSize(TestBangumi.sampleSeason.size)
            }
        }

    @Test
    fun `init loads first season page`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = PopularViewModel(repository)
            advanceUntilIdle()
            assertThat(repository.seasonPageCalls.firstOrNull()?.second).isEqualTo(0)
            viewModel
        }

    @Test
    fun `load failure with empty cache becomes Error state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.seasonResult = Result.Failure(AppError.Network("down"))
            val viewModel = PopularViewModel(repository)

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(PopularUiState.Error::class.java)
            }
        }

    @Test
    fun `full page keeps pagination going and loadMore fetches next page`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.seasonItems.value = TestBangumi.sampleSeason
            repository.seasonResult = Result.Success(BangumiRepository.PAGE_SIZE)
            val viewModel = PopularViewModel(repository)
            advanceUntilIdle()

            viewModel.loadMore()
            advanceUntilIdle()

            assertThat(repository.seasonPageCalls.map { it.second }).containsExactly(0, 1).inOrder()
        }

    @Test
    fun `short page ends pagination`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.seasonItems.value = TestBangumi.sampleSeason
            repository.seasonResult = Result.Success(BangumiRepository.PAGE_SIZE - 1)
            val viewModel = PopularViewModel(repository)
            advanceUntilIdle()

            viewModel.loadMore()
            advanceUntilIdle()

            assertThat(repository.seasonPageCalls.map { it.second }).containsExactly(0)
        }
}
