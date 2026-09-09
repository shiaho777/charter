package dev.charter.feature.anime.popular

import dev.charter.core.model.Bangumi
import dev.charter.core.model.Season
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface PopularUiState {
    data object Loading : PopularUiState

    data class Content(
        val items: ImmutableList<Bangumi>,
        val season: Season,
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = true,
    ) : PopularUiState

    data class Empty(
        val season: Season,
    ) : PopularUiState

    data class Error(
        val message: String,
        val season: Season,
    ) : PopularUiState

    companion object {
        val EmptyItems: ImmutableList<Bangumi> = persistentListOf()
    }
}
