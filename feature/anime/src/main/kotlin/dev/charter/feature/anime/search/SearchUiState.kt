package dev.charter.feature.anime.search

import dev.charter.core.model.Bangumi
import dev.charter.core.model.SearchHistoryEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface SearchUiState {
    /** No active query: show search history. */
    data class Idle(
        val history: ImmutableList<SearchHistoryEntry>,
    ) : SearchUiState

    data class Loading(
        val query: String,
    ) : SearchUiState

    data class Content(
        val query: String,
        val items: ImmutableList<Bangumi>,
    ) : SearchUiState

    data class Empty(
        val query: String,
    ) : SearchUiState

    data class Error(
        val message: String,
        val query: String,
    ) : SearchUiState

    companion object {
        val Initial: SearchUiState = Idle(persistentListOf())
    }
}
