package dev.charter.feature.anime.detail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.model.CollectStatus
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeBangumiRepository
import dev.charter.feature.anime.FakeCollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BangumiDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bangumiRepository = FakeBangumiRepository()
    private val collectionRepository = FakeCollectionRepository()

    private fun viewModel(id: Long = 1L): BangumiDetailViewModel {
        val viewModel = BangumiDetailViewModel(bangumiRepository, collectionRepository)
        viewModel.setBangumiId(id)
        return viewModel
    }

    @Test
    fun `detail and episodes combine into Content`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi()
            bangumiRepository.episodes.value = TestBangumi.sampleEpisodes
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(BangumiDetailUiState.Content::class.java)
                content as BangumiDetailUiState.Content
                assertThat(content.episodes).hasSize(TestBangumi.sampleEpisodes.size)
                assertThat(content.collection).isNull()
            }
        }

    @Test
    fun `load failure without cache becomes Error`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detailResult = Result.Failure(AppError.Network("down"))
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(BangumiDetailUiState.Error::class.java)
            }
        }

    @Test
    fun `setCollectStatus writes through to the collection`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi(id = 7)
            val viewModel = viewModel(id = 7)

            // Subscribe first: stateIn(WhileSubscribed) only feeds uiState.value
            // while an observer is active, and the action reads the current state.
            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(BangumiDetailUiState.Content::class.java)

                viewModel.setCollectStatus(CollectStatus.WATCHING)
                advanceUntilIdle()

                assertThat(collectionRepository.setStatusCalls).containsExactly(7L to CollectStatus.WATCHING)
                assertThat(
                    collectionRepository.entries.value
                        .single()
                        .status,
                ).isEqualTo(CollectStatus.WATCHING)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `remove collection clears the entry`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi(id = 9)
            collectionRepository.entries.value = listOf(TestBangumi.collectionEntry(bangumiId = 9))
            val viewModel = viewModel(id = 9)

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(BangumiDetailUiState.Content::class.java)

                viewModel.removeCollection()
                advanceUntilIdle()

                assertThat(collectionRepository.removeCalls).containsExactly(9L)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
