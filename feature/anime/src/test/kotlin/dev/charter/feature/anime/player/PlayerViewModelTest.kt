package dev.charter.feature.anime.player

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeBangumiRepository
import dev.charter.feature.anime.FakeHistoryRepository
import dev.charter.feature.anime.FakePlaySourceAggregator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val bangumiRepository = FakeBangumiRepository()
    private val historyRepository = FakeHistoryRepository()
    private val aggregator = FakePlaySourceAggregator()

    private fun viewModel(
        bangumiId: Long = 1L,
        episodeSort: Float = 1f,
    ): PlayerViewModel {
        val viewModel = PlayerViewModel(bangumiRepository, historyRepository, aggregator)
        viewModel.setPlayback(bangumiId, "源A", "默认线路", episodeSort)
        return viewModel
    }

    @Test
    fun `playback params produce Content with demo roads`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi()
            bangumiRepository.episodes.value = TestBangumi.sampleEpisodes
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(PlayerUiState.Content::class.java)
                content as PlayerUiState.Content
                assertThat(content.selectedRoad).isEqualTo("默认线路")
                assertThat(content.roads.single().episodes).hasSize(TestBangumi.sampleEpisodes.size)
            }
        }

    @Test
    fun `selectEpisode records watch history`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi()
            bangumiRepository.episodes.value = TestBangumi.sampleEpisodes
            val viewModel = viewModel()

            // Subscribe first: recordWatch reads the current Content state, and
            // stateIn(WhileSubscribed) only populates it under an observer.
            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(PlayerUiState.Content::class.java)

                viewModel.selectEpisode(3f)
                advanceUntilIdle()

                val recorded = historyRepository.recorded.last()
                assertThat(recorded.lastEpisodeSort).isEqualTo(3f)
                assertThat(recorded.progressRatio).isEqualTo(0f)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `markEpisodeFinished records full progress`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi()
            bangumiRepository.episodes.value = TestBangumi.sampleEpisodes
            val viewModel = viewModel(episodeSort = 2f)

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isInstanceOf(PlayerUiState.Content::class.java)

                viewModel.markEpisodeFinished()
                advanceUntilIdle()

                val recorded = historyRepository.recorded.last()
                assertThat(recorded.lastEpisodeSort).isEqualTo(2f)
                assertThat(recorded.progressRatio).isEqualTo(1f)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `selectRoad switches the road without touching history`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            bangumiRepository.detail.value = TestBangumi.bangumi()
            bangumiRepository.episodes.value = TestBangumi.sampleEpisodes
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.selectRoad("备用线路")

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem() as PlayerUiState.Content
                assertThat(content.selectedRoad).isEqualTo("备用线路")
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(historyRepository.recorded).isEmpty()
        }
}
