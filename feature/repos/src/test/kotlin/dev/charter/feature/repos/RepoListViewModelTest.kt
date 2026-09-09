package dev.charter.feature.repos

import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.charter.core.common.result.Result
import dev.charter.core.data.repository.RepoRepository
import dev.charter.core.model.Repo
import dev.charter.core.testing.data.TestRepos
import dev.charter.core.testing.dispatchers.MainDispatcherRule
import dev.charter.feature.repos.list.RepoListUiState
import dev.charter.feature.repos.list.RepoListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RepoListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeRepoRepository()

    @Test
    fun `cached repos become Content state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            fakeRepository.setRepos(TestRepos.sample)
            val viewModel = RepoListViewModel(fakeRepository)

            viewModel.uiState.test {
                advanceUntilIdle()
                val states =
                    cancelAndConsumeRemainingEvents()
                        .filterIsInstance<Event.Item<RepoListUiState>>()
                        .map { it.value }
                assertThat(states.filterIsInstance<RepoListUiState.Content>()).isNotEmpty()
                val content = states.filterIsInstance<RepoListUiState.Content>().last()
                assertThat(content.repos).hasSize(TestRepos.sample.size)
            }
        }

    @Test
    fun `empty cache becomes Empty state`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            fakeRepository.setRepos(emptyList())
            val viewModel = RepoListViewModel(fakeRepository)

            viewModel.uiState.test {
                advanceUntilIdle()
                val states =
                    cancelAndConsumeRemainingEvents()
                        .filterIsInstance<Event.Item<RepoListUiState>>()
                        .map { it.value }
                assertThat(states.filterIsInstance<RepoListUiState.Empty>()).isNotEmpty()
            }
        }

    private class FakeRepoRepository : RepoRepository {
        private val repos = MutableStateFlow<List<Repo>>(emptyList())

        fun setRepos(value: List<Repo>) {
            repos.value = value
        }

        override fun observeRepos(query: String): Flow<List<Repo>> = repos

        override fun observeRepo(id: Long): Flow<Repo?> = repos.map { list -> list.firstOrNull { it.id == id } }

        override suspend fun refresh(query: String): Result<Unit> = Result.Success(Unit)
    }
}
