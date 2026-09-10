package dev.charter.feature.anime.history

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHistoryRepository()

    private fun viewModel() = HistoryViewModel(repository)

    @Test
    fun `entries become Content`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.entries.value = listOf(TestBangumi.historyEntry(bangumiId = 1))
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(HistoryUiState.Content::class.java)
                assertThat((content as HistoryUiState.Content).entries).hasSize(1)
            }
        }

    @Test
    fun `empty library becomes Empty`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                assertThat(expectMostRecentItem()).isEqualTo(HistoryUiState.Empty)
            }
        }

    @Test
    fun `managing mode toggles selection`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.entries.value =
                listOf(
                    TestBangumi.historyEntry(bangumiId = 1),
                    TestBangumi.historyEntry(bangumiId = 2),
                )
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.toggleManaging()
            viewModel.toggleSelected(1L)

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem() as HistoryUiState.Content
                assertThat(content.isManaging).isTrue()
                assertThat(content.selectedIds).containsExactly(1L)
            }
        }

    @Test
    fun `removeSelected deletes each row and exits managing`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.entries.value =
                listOf(
                    TestBangumi.historyEntry(bangumiId = 1),
                    TestBangumi.historyEntry(bangumiId = 2),
                )
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.toggleManaging()
            viewModel.toggleSelected(1L)
            viewModel.toggleSelected(2L)
            viewModel.removeSelected()
            advanceUntilIdle()

            assertThat(repository.entries.value).isEmpty()
            viewModel.uiState.test {
                assertThat(expectMostRecentItem()).isEqualTo(HistoryUiState.Empty)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `clearAll empties the library`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.entries.value = listOf(TestBangumi.historyEntry(bangumiId = 1))
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.clearAll()
            advanceUntilIdle()

            assertThat(repository.clearAllCalls).isEqualTo(1)
        }
}
