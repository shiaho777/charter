package dev.charter.feature.anime.collection

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.model.CollectStatus
import dev.charter.core.testing.data.TestBangumi
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.anime.FakeCollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCollectionRepository()

    private fun viewModel() = CollectionViewModel(repository)

    @Test
    fun `entries become Content`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.entries.value =
                listOf(
                    TestBangumi.collectionEntry(bangumiId = 1, status = CollectStatus.WATCHING),
                    TestBangumi.collectionEntry(bangumiId = 2, status = CollectStatus.WANT),
                )
            val viewModel = viewModel()

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem()
                assertThat(content).isInstanceOf(CollectionUiState.Content::class.java)
                assertThat((content as CollectionUiState.Content).entries).hasSize(2)
            }
        }

    @Test
    fun `filter narrows to one status`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.entries.value =
                listOf(
                    TestBangumi.collectionEntry(bangumiId = 1, status = CollectStatus.WATCHING),
                    TestBangumi.collectionEntry(bangumiId = 2, status = CollectStatus.WANT),
                )
            val viewModel = viewModel()

            viewModel.setFilter(CollectStatus.WANT)

            viewModel.uiState.test {
                advanceUntilIdle()
                val content = expectMostRecentItem() as CollectionUiState.Content
                assertThat(content.entries).hasSize(1)
                assertThat(content.entries.single().bangumiId).isEqualTo(2L)
            }
        }

    @Test
    fun `setStatus writes through the repository`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val entry = TestBangumi.collectionEntry(bangumiId = 3, status = CollectStatus.WANT)
            repository.entries.value = listOf(entry)
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.setStatus(entry, CollectStatus.WATCHING)
            advanceUntilIdle()

            assertThat(repository.setStatusCalls).hasSize(1)
            assertThat(repository.setStatusCalls.single().second).isEqualTo(CollectStatus.WATCHING)
            viewModel.uiState.test {
                val content = expectMostRecentItem() as CollectionUiState.Content
                assertThat(content.pendingIds).isEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `write failure surfaces a message and clears pending`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            repository.writeResult = Result.Failure(AppError.Storage("disk full"))
            val entry = TestBangumi.collectionEntry(bangumiId = 4)
            repository.entries.value = listOf(entry)
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.messages.test {
                viewModel.setStatus(entry, CollectStatus.WATCHED)
                advanceUntilIdle()
                assertThat(awaitItem()).isNotEmpty()
            }
            viewModel.uiState.test {
                val content = expectMostRecentItem() as CollectionUiState.Content
                assertThat(content.pendingIds).isEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }
}
