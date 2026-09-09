package dev.charter.feature.repos.list

import dev.charter.core.model.Repo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface RepoListUiState {
    data object Loading : RepoListUiState

    data class Content(
        val repos: ImmutableList<Repo>,
        val isRefreshing: Boolean = false,
    ) : RepoListUiState

    data object Empty : RepoListUiState

    data class Error(
        val message: String,
    ) : RepoListUiState

    companion object {
        val Initial: RepoListUiState = Loading
        val EmptyContent: ImmutableList<Repo> = persistentListOf()
    }
}
